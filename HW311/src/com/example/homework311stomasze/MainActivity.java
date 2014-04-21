package com.example.homework311stomasze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    String kTag = "MainActivity";
    ListView list;
    ListViewAdapter adapter;
    ArrayList<HashMap<String, String>> mArticleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i(kTag, "onResume()");
        showAllArticles();

    }

    public void showAllArticles() {
        // Show all the Articles sorted by Title name
        String URL = ArticlesProvider.URL;
        Uri articles = Uri.parse(URL);
        // Delete First
        int count = getContentResolver().delete(articles, null, null);
        // Get threw new Content
        Cursor c =
                getContentResolver().query(articles, null, null, null,
                        ArticlesProvider.TITLE);

        String result = "Results:";

        if (!c.moveToFirst()) {
            Toast.makeText(this, result + " no content yet!", Toast.LENGTH_LONG)
                    .show();
        }
        else {
            mArticleList = null;
            mArticleList = new ArrayList<HashMap<String, String>>();
            do {
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();
                // adding each child node to HashMap key => value
                map.put(ArticlesProvider.ID,
                        c.getString(c.getColumnIndex(ArticlesProvider.ID)));
                map.put(ArticlesProvider.CONTENT,
                        c.getString(c.getColumnIndex(ArticlesProvider.CONTENT)));
                map.put(ArticlesProvider.TITLE,
                        c.getString(c.getColumnIndex(ArticlesProvider.TITLE)));
                map.put(ArticlesProvider.DATE,
                        c.getString(c.getColumnIndex(ArticlesProvider.DATE)));
                map.put(ArticlesProvider.ICON,
                        c.getString(c.getColumnIndex(ArticlesProvider.ICON)));


                // adding HashList to ArrayList
                mArticleList.add(map);
            }
            while (c.moveToNext());
            // Log.i(kTag, result);
            list = (ListView) findViewById(R.id.list);

            // Getting adapter by passing xml data ArrayList
            adapter = new ListViewAdapter(this, mArticleList);
            list.setAdapter(adapter);

            // Click event for single list row
            list.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    HashMap<String, String> map = mArticleList.get(position);


                    String content = (String) map.get(ArticlesProvider.CONTENT);
                    String title = (String) map.get(ArticlesProvider.TITLE);
                    String icon = (String) map.get(ArticlesProvider.ICON);
                    String date = (String) map.get(ArticlesProvider.DATE);

                    Intent myIntent =
                            new Intent(view.getContext(),
                                    ListItemActivity.class);
                    myIntent.putExtra(ArticlesProvider.CONTENT, content);
                    myIntent.putExtra(ArticlesProvider.TITLE, title);
                    myIntent.putExtra(ArticlesProvider.ICON, icon);
                    myIntent.putExtra(ArticlesProvider.DATE, date);
                    startActivityForResult(myIntent, 0);
                }
            });
        }



    }

}
