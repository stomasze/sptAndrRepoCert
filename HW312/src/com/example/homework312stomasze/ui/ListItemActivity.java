package com.example.homework312stomasze.ui;

import com.example.homework312stomasze.R;
import com.example.homework312stomasze.R.id;
import com.example.homework312stomasze.R.layout;
import com.example.homework312stomasze.data.ImageLoader;
import com.example.homework312stomasze.providers.ArticlesProvider;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class ListItemActivity extends Activity {

    private static String title = "";
    private static String date = "";
    private static String icon = "";
    private static String content = "";
    public ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item);
        imageLoader = new ImageLoader(this.getApplicationContext());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString(ArticlesProvider.TITLE);
            date = extras.getString(ArticlesProvider.DATE);
            icon = extras.getString(ArticlesProvider.ICON);
            content = extras.getString(ArticlesProvider.CONTENT);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadData(content, "text/html", "UTF-8");
        // final TextView mTvContent = (TextView) findViewById(R.id.item_content);
        // mTvContent.setText(content);
        final TextView mTvTitle = (TextView) findViewById(R.id.item_title);
        mTvTitle.setText("Title: " + title);
        final TextView mTvDate = (TextView) findViewById(R.id.item_date);
        mTvDate.setText("Date: " + date);
        // TEST icon = "http://cdn1.scringo.com/resources/v1/android_icon_256.png";
        if (!icon.equals("")) {
            final ImageView thumb_image =
                    (ImageView) findViewById(R.id.item_icon_image); // thumb image
            imageLoader.DisplayImage(icon, thumb_image);
        }
    }


}
