package com.example.homework314stomasze.ui;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.homework314stomasze.R;
import com.example.homework314stomasze.R.id;
import com.example.homework314stomasze.R.layout;
import com.example.homework314stomasze.R.menu;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

    private final String kTag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide titlebar of application
        // must be before setting the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // hide statusbar of Android
        // could also be done later
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        // Fragment Stuff
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) { return true; }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private final String kTag = "PlaceholderFragment";
        private static EditText myZipCode;
        private static String editTextField;
        private static Spinner spinner;
        private static ProgressBar progBar;
        private static UIErrorPopUp errorPopHandler;
        private static UISpinnerControl mSpinnerHandler;
        private static UIUpdater mHandler;
        private static UIWeather mHandlerWeather;
        private static final String SeattleWoeid = "2490383";
        private static CurrWeather currWeather = null;
        private static ArrayList<FutureWeather> futureWeather = null;
        private static Context context;
        private static TextView locText;
        private static TextView condText;
        private static ImageView image;
        private static TextView []futureDay;
        private static TextView []futureWea;
        private static TextView []futureWeaLow;
        private static TextView []futureWeaHigh;
        private static ImageView []futureImage;
        private static View rootView;
        private static boolean boolFlag = true;
        private static ArrayList<ZipCodeResults> zipCodeResults = null;

        public static class UIErrorPopUp extends Handler {
            private final WeakReference<PlaceholderFragment> mService;

            UIErrorPopUp(PlaceholderFragment service) {
                mService = new WeakReference<PlaceholderFragment>(service);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String aResponse = msg.getData().getString("message");
                if (aResponse.equals("1")) {
                    mService.get().popUpErroDialog();
                }
                else if (aResponse.equals("2")) {
                    mService.get().networkNotAvailableError();
                }

            }
        }

        public static class UISpinnerControl extends Handler {
            private final WeakReference<PlaceholderFragment> mService;

            UISpinnerControl(PlaceholderFragment service) {
                mService = new WeakReference<PlaceholderFragment>(service);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progBar.setVisibility(View.VISIBLE);
            }
        }

        public static class UIUpdater extends Handler {
            private final WeakReference<PlaceholderFragment> mService;

            UIUpdater(PlaceholderFragment service) {
                mService = new WeakReference<PlaceholderFragment>(service);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String[] selections = new String[zipCodeResults.size() + 1];
                selections[0] = " ";
                Iterator<ZipCodeResults> it = zipCodeResults.iterator();
                for (int i = 1; i < zipCodeResults.size() + 1; i++) {
                    ZipCodeResults currArticle = it.next();
                    selections[i] =
                            currArticle.state + ", " + currArticle.country;
                }
                progBar.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);

                ArrayAdapter<String> myAdapter =
                        new ArrayAdapter<String>(spinner.getContext(),
                                android.R.layout.simple_spinner_item,
                                selections);
                myAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(myAdapter);
                spinner.performClick();
            }
        }
        public class UIWeather extends Handler {
            private final WeakReference<PlaceholderFragment> mService;

            UIWeather(PlaceholderFragment service) {
                mService = new WeakReference<PlaceholderFragment>(service);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // Hiding Keyboard
                InputMethodManager in =
                        (InputMethodManager) context
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(
                        myZipCode.getApplicationWindowToken(), 0);

                mService.get().displayWeather();
            }
        }

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView =
                    inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }

        @Override
        public void onDestroy() {
            // TODO Auto-generated method stub
            super.onDestroy();
        }

        @Override
        public void onDestroyView() {
            // TODO Auto-generated method stub
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            setIDs();
        }


        public void setIDs() {
            errorPopHandler = new UIErrorPopUp(this);
            mHandler = new UIUpdater(this);
            mHandlerWeather = new UIWeather(this);
            mSpinnerHandler = new UISpinnerControl(this);

            // Future Weather
            
            futureDay = new TextView[4];
            futureWea = new TextView[4];
            futureWeaLow = new TextView[4];
            futureWeaHigh = new TextView[4];
            futureImage = new ImageView [4];

            futureDay[0] = (TextView) rootView.findViewById(R.id.futureDay1);
            futureWea[0] = (TextView) rootView.findViewById(R.id.future1);
            futureWeaLow[0] = (TextView) rootView.findViewById(R.id.futureLow1);
            futureWeaHigh[0] = (TextView) rootView.findViewById(R.id.futureHigh1);
            futureImage[0] = (ImageView) rootView.findViewById(R.id.futureImgIcon1);
            
            futureDay[1] = (TextView) rootView.findViewById(R.id.futureDay2);
            futureWea[1] = (TextView) rootView.findViewById(R.id.future2);
            futureWeaLow[1] = (TextView) rootView.findViewById(R.id.futureLow2);
            futureWeaHigh[1] = (TextView) rootView.findViewById(R.id.futureHigh2);
            futureImage[1] = (ImageView) rootView.findViewById(R.id.futureImgIcon2);

            futureDay[2] = (TextView) rootView.findViewById(R.id.futureDay3);
            futureWea[2] = (TextView) rootView.findViewById(R.id.future3);
            futureWeaLow[2] = (TextView) rootView.findViewById(R.id.futureLow3);
            futureWeaHigh[2] = (TextView) rootView.findViewById(R.id.futureHigh3);
            futureImage[2] = (ImageView) rootView.findViewById(R.id.futureImgIcon3);

            futureDay[3] = (TextView) rootView.findViewById(R.id.futureDay4);
            futureWea[3] = (TextView) rootView.findViewById(R.id.future4);
            futureWeaLow[3] = (TextView) rootView.findViewById(R.id.futureLow4);
            futureWeaHigh[3] = (TextView) rootView.findViewById(R.id.futureHigh4);
            futureImage[3] = (ImageView) rootView.findViewById(R.id.futureImgIcon4);

            // Condtion
            condText = (TextView) rootView.findViewById(R.id.condition);


            // Location
            locText = (TextView) rootView.findViewById(R.id.location);

            image = (ImageView) rootView.findViewById(R.id.imgIcon);
            // Getting Spinner
            spinner = (Spinner) rootView.findViewById(R.id.spinner);
            spinner.setVisibility(View.GONE);
            spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
            // Progress Bar
            progBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            progBar.setVisibility(View.GONE);

            this.context = this.getActivity();
            // EditText
            myZipCode = (EditText) rootView.findViewById(R.id.zipCode);
            myZipCode.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                    editTextField = myZipCode.getText().toString();
                    // If the user Entered the full zip code
                    if (myZipCode.getText().toString().length() >= 5
                            && boolFlag) {
                        boolFlag = false;
                        new MyQueryYahooPlaceTask().execute();
                        // Hiding Keyboard
                        InputMethodManager in =
                                (InputMethodManager) context
                                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(
                                myZipCode.getApplicationWindowToken(), 0);
                    }
                    else if (myZipCode.getText().toString().length() < 5) {
                        boolFlag = true;
                    }

                }

                public void beforeTextChanged(CharSequence s, int start,
                        int count, int after) {}

                public void onTextChanged(CharSequence s, int start,
                        int before, int count) {}
            });
            if (currWeather != null && futureWeather != null
                    && !futureWeather.isEmpty()) {
                // Hiding Keyboard
                InputMethodManager in =
                        (InputMethodManager) context
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(
                        myZipCode.getApplicationWindowToken(), 0);
                displayWeather();
            }
            else {
                if (!isNetworkAvailable()) {
                    Message msgObj = errorPopHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", "2");
                    msgObj.setData(b);
                    errorPopHandler.sendMessage(msgObj);
                    // errorPopHandler.post(msgObj);
                    // networkNotAvailableError();
                    return;
                }
                new MyQueryYahooDefaultWeatherTask().execute();
            }

        }

        private class MyQueryYahooPlaceTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... arg0) {
                getListOfCitiesBasedOnZipCode();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

        }
        private class MyQueryYahooWeatherTask
                extends AsyncTask<Void, Void, Void> {
            public int position = 0;

            public MyQueryYahooWeatherTask(int pos) {
                position = pos;
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                addressChosen(position);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

        }
        private class MyQueryYahooDefaultWeatherTask
                extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... arg0) {
                getWeather(SeattleWoeid);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

        }
        class ZipCodeResults {
            public String woeid;
            public String state;
            public String country;
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo =
                    connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        public void getListOfCitiesBasedOnZipCode() {
            if (!isNetworkAvailable()) {
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "2");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);

                // networkNotAvailableError();
                return;
            }
            // http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.places%20where%20text=%2298028%22&format=xml
            String qureyURL =
                    "http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.places%20where%20text=%22"
                            + editTextField + "%22&format=xml";
            zipCodeResults = null;
            zipCodeResults = new ArrayList<ZipCodeResults>();
            ZipCodeResults zipCodeResult = null;
            mSpinnerHandler.post(null);

            // quare the zip code
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(qureyURL);
            try {
                HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

                if (httpEntity != null) {
                    InputStream inputStream = httpEntity.getContent();
                    Reader in = new InputStreamReader(inputStream);
                    XmlPullParserFactory factory =
                            XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(in);
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String name = null;
                        String value = null;
                        switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            name = xpp.getName();
                            if (name.equals("place")) {
                                zipCodeResult = new ZipCodeResults();
                                zipCodeResult.woeid = "";
                                zipCodeResult.state = "";
                                zipCodeResult.country = "";
                            }
                            else if (name.equals("woeid")) {
                                value = xpp.nextText();
                                zipCodeResult.woeid = value;
                                // Log.e(kTag, "START_TAG = " + name + " VALUE = "
                                // + value);
                            }
                            else if (name.equals("country")) {
                                value = xpp.nextText();
                                zipCodeResult.country = value;
                                // Log.e(kTag, "START_TAG = " + name + " VALUE = "
                                // + value);
                            }
                            else if (name.equals("admin1")) {
                                value = xpp.nextText();
                                zipCodeResult.state = value;
                                // Log.e(kTag, "START_TAG = " + name + " VALUE = "
                                // + value);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            name = xpp.getName();
                            if (name.equalsIgnoreCase("place")
                                    && zipCodeResult != null) {
                                zipCodeResults.add(zipCodeResult);
                            }
                        }
                        eventType = xpp.next();
                    }
                }
            }
            catch (XmlPullParserException e) {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);

            }
            catch (ClientProtocolException e) {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }
            catch (IOException e) {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }
            if (zipCodeResults != null && !zipCodeResults.isEmpty()) {

                // If there is more than 1 address
                if (zipCodeResults.size() > 1) {
                    mHandler.post(null);
                }
                else if (zipCodeResults.size() > 0) {
                    // Got at least one address
                    new MyQueryYahooWeatherTask(0).execute();// addressChosen(0);
                }
                else {
                    // popUpErroDialog();
                    Message msgObj = errorPopHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", "1");
                    msgObj.setData(b);
                    errorPopHandler.sendMessage(msgObj);
                }
            }
            else {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }
        }



        class CurrWeather {
            public String condText;
            public String condCode;
            public String condTempC;
            public String locCity;
            public String locState;
            public String locCountry;
        }
        class FutureWeather {
            public String day;
            public String lowTempC;
            public String highTempC;
            public String condText;
            public String condCode;
        }

        public void displayWeather() {
            // Hiding Keyboard
            InputMethodManager in =
                    (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(myZipCode.getApplicationWindowToken(), 0);

            String cond =
                    currWeather.condText + "  Temp: " + currWeather.condTempC
                            + " \u2103 / "
                            + convertCelsiusToFahrenheit(currWeather.condTempC)
                            + " \u2109";
            condText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            condText.setText(cond);
            String loc =
                    currWeather.locCity + " " + currWeather.locState + " "
                            + currWeather.locCountry;
            locText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            locText.setText(loc);

            // Bitmap bmp=BitmapFactory.decodeByteArray(blob,0,blob.length);
            String imgName = "weather" + currWeather.condCode;


            image.setImageDrawable(getResources().getDrawable(
                    getResources().getIdentifier(imgName, "drawable",
                            context.getPackageName())));

            int totalFutureWeather = futureWeather.size();
            
            for(int i = 0; i</*1;i++){*/Math.min(totalFutureWeather, 4);i++){
                FutureWeather day = futureWeather.get(i);
                futureDay[i].setText(day.day);
                futureDay[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                futureDay[i].setTextColor(Color.GREEN);
                futureWea[i].setText(day.condText);
                futureWeaLow[i].setText("Low:"+day.lowTempC+ " \u2103 / "
                        + convertCelsiusToFahrenheit(day.lowTempC)
                        + " \u2109");
                futureWeaHigh[i].setText("High:"+day.highTempC+ " \u2103 / "
                        + convertCelsiusToFahrenheit(day.highTempC)
                        + " \u2109");
                imgName = "weather" + day.condCode;
                futureImage[i].setImageDrawable(getResources().getDrawable(
                        getResources().getIdentifier(imgName, "drawable",
                                context.getPackageName())));
            }

        }

        // Converts to fahrenheit
        private int convertCelsiusToFahrenheit(String celsius) {
            return ((Integer.parseInt(celsius) * 9) / 5) + 32;
        }



        public void addressChosen(int position) {
            ZipCodeResults address = zipCodeResults.get(position);
            // http://weather.yahooapis.com/forecastrss?w=12798902&u=c&#8221
            getWeather(address.woeid);

        }

        public void getWeather(String woeid) {
            currWeather = null;
            currWeather = new CurrWeather();
            futureWeather = null;
            futureWeather = new ArrayList<FutureWeather>();

            // Getting the weather in Celcius
            String urlAddressC =
                    "http://weather.yahooapis.com/forecastrss?w=" + woeid
                            + "&u=c&#8221";

            // quare the zip code
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlAddressC);
            try {
                HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

                if (httpEntity != null) {
                    InputStream inputStream = httpEntity.getContent();
                    Reader in = new InputStreamReader(inputStream);
                    XmlPullParserFactory factory =
                            XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(in);
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String name = null;
                        String value = null;
                        switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            name = xpp.getName();
                            // Log.e(kTag, name);
                            if (name.equals("condition")) {
                                currWeather.condText = xpp.getAttributeValue(0);
                                currWeather.condCode = xpp.getAttributeValue(1);
                                currWeather.condTempC =
                                        xpp.getAttributeValue(2);
                            }
                            if (name.equals("location")) {
                                currWeather.locCity = xpp.getAttributeValue(0);
                                currWeather.locState = xpp.getAttributeValue(1);
                                currWeather.locCountry =
                                        xpp.getAttributeValue(2);
                            }
                            else if (name.equals("forecast")) {
                                FutureWeather collection = new FutureWeather();
                                collection.day = xpp.getAttributeValue(0);
                                collection.lowTempC = xpp.getAttributeValue(2);
                                collection.highTempC = xpp.getAttributeValue(3);
                                collection.condText = xpp.getAttributeValue(4);
                                collection.condCode = xpp.getAttributeValue(5);
                                futureWeather.add(collection);
                            }


                            break;
                        case XmlPullParser.END_TAG:
                        }
                        eventType = xpp.next();
                    }
                }
            }
            catch (XmlPullParserException e) {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }
            catch (ClientProtocolException e) {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }
            catch (IOException e) {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }
            if (currWeather != null && futureWeather != null
                    && !futureWeather.isEmpty()) {
                // blob = getImage(currWeather.condCode);
                mHandlerWeather.post(null);

            }
            else {
                // popUpErroDialog();
                Message msgObj = errorPopHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "1");
                msgObj.setData(b);
                errorPopHandler.sendMessage(msgObj);
            }


        }

        public void networkNotAvailableError() {
            new AlertDialog.Builder(this.getActivity())
                    .setTitle("Network Not Available!")
                    .setMessage(
                            "Network IS NOT avilable, please turn it on prior to continuing")
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    getListOfCitiesBasedOnZipCode();
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // PopUp Error Dialog
        public void popUpErroDialog() {
            new AlertDialog.Builder(this.getActivity())
                    .setTitle("No Weather Found!")
                    .setMessage(
                            "Couldn't get the Weather, would you like to try again")
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    getListOfCitiesBasedOnZipCode();
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        public class MyOnItemSelectedListener implements OnItemSelectedListener {

            @Override
            public void onItemSelected(AdapterView parent, View view, int pos,
                    long id) {
                if (pos != 0) {
                    spinner.setVisibility(View.GONE);

                    new MyQueryYahooWeatherTask(pos - 1).execute();
                    // addressChosen(pos - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView parent) {

            }
        }

    }

}
