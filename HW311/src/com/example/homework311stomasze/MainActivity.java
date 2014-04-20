package com.example.homework311stomasze;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends Activity {
    String kTag = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showAllArticles();

    }



    
    public void showAllArticles() {
        // Show all the Articles sorted by Title name
        String URL = ArticlesProvider.URL;
        Uri articles = Uri.parse(URL);
        // Delete First
        int count = getContentResolver().delete(articles, null, null);
        // Get thew new Content
        Cursor c = getContentResolver().query(articles, null, null, null, ArticlesProvider.TITLE);
        
        String result = "Results:";
        
        if (!c.moveToFirst()) {
            Toast.makeText(this, result+" no content yet!", Toast.LENGTH_LONG).show();
        }else{
            do{
              result = result + "\nID = " +c.getString(c.getColumnIndex(ArticlesProvider.ID))+
                                "\nCONTENT = "+c.getString(c.getColumnIndex(ArticlesProvider.CONTENT)) + 
                                "\nTITLE = " +  c.getString(c.getColumnIndex(ArticlesProvider.TITLE)) + 
                                 "\nDATE = " + c.getString(c.getColumnIndex(ArticlesProvider.DATE));
            } while (c.moveToNext());
           Log.i(kTag, result);
        }
       
     }
    
    

}
