package com.example.homework312stomasze.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.homework312stomasze.R;
import com.example.homework312stomasze.providers.ArticlesProvider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorListener {
    private static String kTag = "MainActivity";
    private static ListView list;
    private static ListViewAdapter adapter;
    private static LinearLayout layout;

    private static ArrayList<HashMap<String, String>> mArticleList = null;
    private static SensorManager sensorMgr;

    private static final int SHAKE_THRESHOLD = 2000;
    private static long lastUpdate = 0;
    private static float last_x = 0;
    private static float last_y = 0;
    private static float last_z = 0;
    private static boolean updatingListView = false;

    static private Cursor c;
    private BroadcastReceiver receiver;
    private UIUpdater mHandler;

    public static class UIUpdater extends Handler {
        private final WeakReference<MainActivity> mService;

        UIUpdater(MainActivity service) {
            mService = new WeakReference<MainActivity>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity service = mService.get();
            if (service != null) {
                Log.e(kTag, "CALLING UPDATE LIST");
                service.updateList();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new UIUpdater(this);
        // REgister Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ArticlesProvider.BROADCAST_ALL_DATA_READY);

        // Receiver Action
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(kTag, "GOT RECEIVE");
                mHandler.post(null);
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(kTag, "onResume()");

        layout = (LinearLayout) findViewById(R.id.progressbar_view);
        layout.setVisibility(View.GONE);
        list = (ListView) findViewById(R.id.list);
        // new UpdateAricles().execute();
        if (!updatingListView && mArticleList == null) {
            showAllArticles();
        }
        else if (!updatingListView) {
            printView();
        }
        sensorMgr = null;
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void showAllArticles() {
        // Show all the Articles sorted by Title name
        String URL = ArticlesProvider.URL;
        Uri articles = Uri.parse(URL);
        // Delete First
        int count = getContentResolver().delete(articles, null, null);

        // getContentResolver().getNewArticles();
        // Get threw new Content
        c =
                getContentResolver().query(articles, null, null, null,
                        ArticlesProvider.TITLE);

        Log.e(kTag,"Getting Data");
        // String result = "Results:";
        //
        // if (!c.moveToFirst()) {
        // Toast.makeText(this, result + " no content yet!", Toast.LENGTH_LONG)
        // .show();
        // }
        // else {
        // //updateList();
        //
        // }
    }

    public void updateList() {
        Log.e(kTag,"UpdateList");
        // Show all the Articles sorted by Title name
        String URL = ArticlesProvider.URL;
        Uri articles = Uri.parse(URL);
        c =
                getContentResolver().query(articles, null, null, null,
                        ArticlesProvider.TITLE);
        // layout.setVisibility(View.VISIBLE);
        if (!c.moveToFirst()) {
            Toast.makeText(this, " no content yet!", Toast.LENGTH_LONG).show();
        }
        else {
            // Ensure that we lock up any other updates until those are finished
            updatingListView = true;
            // Re-Initializing ListView
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
            printView();
        }

    }

    public void printView() {

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
                if (!updatingListView) {
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
            }
        });
        adapter.notifyDataSetChanged();
        // Indicate that it is Done updating
        updatingListView = false;
        // layout.setVisibility(View.GONE);

    }

    class UpdateAricles extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            layout.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            layout.setVisibility(View.GONE);
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // showAllArticles();
            try {
                while (updatingListView)
                    Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = values[SensorManager.DATA_X];
                float y = values[SensorManager.DATA_Y];
                float z = values[SensorManager.DATA_Z];

                float speed =
                        Math.abs(x + y + z - last_x - last_y - last_z)
                                / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD && !updatingListView) {
                    updatingListView = true;
                    new UpdateAricles().execute();
                    showAllArticles();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }



    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
        // NOTHING HERE
    }

}
