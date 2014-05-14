package com.example.homework312stomasze.providers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

public class ArticlesProvider extends ContentProvider {
    String kTag = "ArticlesProvider";

    // BroadCast Receiver to be send to
    static public final String BROADCAST_ALL_DATA_READY =
            "com.example.homework312stomasze.DATA_READY";

    // fields for my content provider
    static public final String PROVIDER_NAME =
            "com.example.homework312stomasze.providers";
    static public final String URL = "content://" + PROVIDER_NAME + "/articles";
    static public final Uri CONTENT_URI = Uri.parse(URL);

    // fields for the database
    static public final String ID = "Id";
    static public final String ARTICLES_TAG = "articles";
    static public final String ARTICLES_ITEM = "item";
    static public final String CONTENT = "content";
    static public final String ICON = "icon";
    static public final String TITLE = "title";
    static public final String DATE = "date";

    // integer values used in content URI
    static public final int ARTICLES = 1;
    static public final int ARTICLES_ID = 2;

    DBHelper dbHelper;

    // projection map for a query
    private static HashMap<String, String> ArticlesMap;

    // maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "articles", ARTICLES);
        uriMatcher.addURI(PROVIDER_NAME, "articles/#", ARTICLES_ID);
    }

    // database declarations
    private SQLiteDatabase database;
    static final String DATABASE_NAME = "ArticlesList";
    static final String TABLE_NAME = "articlesTable";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + ID
            + " integer primary key autoincrement, " + CONTENT + " text, "
            + ICON + " text," + TITLE + " text," + DATE + " text" + ");";

    private static Context context;



    // class that creates and manages the provider's database
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates The Database
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DBHelper.class.getName(), "Upgrading database from version "
                    + oldVersion + " to " + newVersion
                    + ". Old data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

    }

    @Override
    public boolean onCreate() {
        context = getContext();
        dbHelper = new DBHelper(context);
        // permissions to be writable
        database = dbHelper.getWritableDatabase();
        if (database == null)
            return false;
        else
            return true;
    }

    public void getNewArticles() {
        // Populate first from the XML FILE
        new ArticleListAsyncTask().execute();
    }

    private static boolean boolGetNewArticles = true;
    private static ArticleListAsyncTask myTask;

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        Log.e(kTag, "Quary");
        // Populate Database
        if (boolGetNewArticles) {
            if (myTask != null) {
                myTask.cancel(true);
                myTask = null;
            }
            myTask = new ArticleListAsyncTask();
            myTask.execute();
            boolGetNewArticles = false;
            Log.e(kTag, "Getting New Articles");

        }
        else {
            Log.e(kTag, "Getting the cursor");

            boolGetNewArticles = true;
        }
        // SQlite QUotery
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // the TABLE_NAME to query on
        queryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
        // maps all database column names
        case ARTICLES:
            queryBuilder.setProjectionMap(ArticlesMap);
            break;
        case ARTICLES_ID:
            queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            // No sorting-> sort on names by default
            sortOrder = TITLE;
        }
        Cursor cursor =
                queryBuilder.query(database, projection, selection,
                        selectionArgs, null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return localInsert(uri, values);
    }

    public Uri localInsert(Uri uri, ContentValues values) {
        long row = database.insert(TABLE_NAME, "", values);

        // If record is added successfully
        if (row > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into " + uri);
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
        case ARTICLES:
            // delete all the records of the table
            count = database.delete(TABLE_NAME, selection, selectionArgs);
            break;
        case ARTICLES_ID:
            String id = uri.getLastPathSegment(); // gets the id
            count =
                    database.delete(TABLE_NAME,
                            ID
                                    + " = "
                                    + id
                                    + (!TextUtils.isEmpty(selection) ? " AND ("
                                            + selection + ')' : ""),
                            selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
        case ARTICLES:
            count =
                    database.update(TABLE_NAME, values, selection,
                            selectionArgs);
            break;
        case ARTICLES_ID:
            count =
                    database.update(
                            TABLE_NAME,
                            values,
                            ID
                                    + " = "
                                    + uri.getLastPathSegment()
                                    + (!TextUtils.isEmpty(selection) ? " AND ("
                                            + selection + ')' : ""),
                            selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private class ArticleListAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.e(kTag, "ArticleListAsync");
            populateDataBase("https://news.google.com/news/section?topic=w&output=rss");
            populateDataBase("http://news.yahoo.com/rss/world/");
            Log.e(kTag, "After AsyncTask");
            // Send BroadCast that we are done with the
            Intent intent = new Intent();
            intent.setAction(BROADCAST_ALL_DATA_READY);
            context.sendBroadcast(intent);
            Log.e(kTag, "Send BroadCast");
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {}
    }

    public void populateDataBase(String data) {
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            InputStream in = getHTTPInputStream(data);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            parseXML(parser, data);
            in.close();
        }
        catch (Throwable t) {
            Log.e(kTag, t + "");
        }

    }

    protected InputStream getHTTPInputStream(String site) throws IOException {

        InputStream inputStream = null;

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(site);

        HttpResponse response = client.execute(httpGet);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode != 200) {
            // We have had some kind of error
            Log.e("Error", "HTTP Error: " + statusCode);
            return null;
        }

        HttpEntity entity = response.getEntity();
        inputStream = entity.getContent();

        return inputStream;
    }

    private void parseXML(XmlPullParser parser, String data)
            throws XmlPullParserException, IOException {
        ArrayList<Articles> articles = new ArrayList<Articles>();
        int eventType = parser.getEventType();
        Articles currentArticle = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                // articles = new ArrayList<Articles>();
                break;
            case XmlPullParser.START_TAG:
                name = parser.getName();
                if (name.equals(ARTICLES_TAG)) {

                }
                else if (name.equals(ARTICLES_ITEM)) {
                    currentArticle = new Articles();
                    currentArticle.content = "";
                    currentArticle.title = "";
                    currentArticle.icon = "";
                    currentArticle.date = "";
                }
                else if (currentArticle != null) {
                    if (name.equals(CONTENT) || name.equals("description")) {
                        if (data == "http://news.yahoo.com/rss/world/") {
                            String str = parser.nextText();
                            Spanned sp = Html.fromHtml(str);
                            currentArticle.content = sp.toString();
                            currentArticle.icon = getIcon(str);
                        }
                        else {
                            String str = parser.nextText();
                            Spanned sp = Html.fromHtml(str);
                            currentArticle.content = sp.toString();
                            // currentArticle.icon = getIcon(str);
                        }
                    }
                    // else if (name.equals(ICON) || name.equals("guid")) {
                    // eventType = parser.next();
                    // eventType = parser.next();
                    // name = parser.getName();
                    // if(name.equals("media:content"))
                    // currentArticle.icon = parser.nextText();
                    // }
                    else if (name.equals(TITLE)) {
                        currentArticle.title = parser.nextText();
                    }
                    else if (name.equals(DATE) || name.equals("pubDate")) {
                        currentArticle.date = parser.nextText();
                    }
                }
                break;
            case XmlPullParser.END_TAG:
                name = parser.getName();
                if (name.equalsIgnoreCase(ARTICLES_ITEM)
                        && currentArticle != null) {
                    articles.add(currentArticle);
                }
            }
            eventType = parser.next();
        }

        printArticles(articles);
    }

    public String getContent(String htmlData) {

        String content = "";

        int first = htmlData.indexOf("</a>");
        int last = htmlData.indexOf("</p><br clear=\"all\"/");
        content = htmlData.substring(first + 4, last);
        return content;
    }

    public String getIcon(String htmlData) {

        String icon = "";

        int first = htmlData.indexOf("img src=");
        int last = htmlData.indexOf("\" width=");
        if (first != -1 && last != -1)
            icon = htmlData.substring(first + 9, last);

        return icon;
    }

    class Articles {
        public String content;
        public String icon;
        public String title;
        public String date;
    }

    private void printArticles(ArrayList<Articles> articles) {
        // String content = "";
        Iterator<Articles> it = articles.iterator();
        while (it.hasNext()) {
            Articles currArticle = it.next();
            // Adding stuff to Database
            ContentValues values = new ContentValues();
            values.put(CONTENT, currArticle.content);
            values.put(TITLE, currArticle.title);
            values.put(ICON, currArticle.icon);
            values.put(DATE, currArticle.date);
            Uri uri = localInsert(CONTENT_URI, values);
            // Log.e(kTag, "INSERTED:");
            // Log.e(kTag, uri.toString());
        }


    }

}
