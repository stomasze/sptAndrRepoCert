package com.example.homework311stomasze;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ArticlesProvider extends ContentProvider {
    String kTag = "ArticlesProvider";
    // fields for my content provider
    static final String PROVIDER_NAME = "com.example.homework311stomasze";
    static final String URL = "content://" + PROVIDER_NAME + "/articles";
    static final Uri CONTENT_URI = Uri.parse(URL);

    // fields for the database
    static final String ID = "Id";
    static final String ARTICLES_TAG = "articles";
    static final String ARTICLES_ITEM = "item";
    static final String CONTENT = "content";
    static final String ICON = "icon";
    static final String TITLE = "title";
    static final String DATE = "date";

    // integer values used in content URI
    static final int ARTICLES = 1;
    static final int ARTICLES_ID = 2;

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

    Context context;

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

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // Populate first from the XML FILE
        populateDataBase();
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


    public void populateDataBase() {
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            InputStream in =
                    context.getResources().openRawResource(R.raw.hrd314_data);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            parseXML(parser);
            in.close();
        }
        catch (Throwable t) {
            Log.e("Error", t + "");
        }


    }

    private void parseXML(XmlPullParser parser) throws XmlPullParserException,
            IOException {
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
                    if (name.equals(CONTENT)) {
                        currentArticle.content = parser.nextText();
                    }
                    else if (name.equals(ICON)) {
                        currentArticle.icon = parser.nextText();
                    }
                    else if (name.equals(TITLE)) {
                        currentArticle.title = parser.nextText();
                    }
                    else if (name.equals(DATE)) {
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
