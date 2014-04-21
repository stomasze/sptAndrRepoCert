package com.example.homework311stomasze;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;

    public ListViewAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        inflater =
                (LayoutInflater) activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.list_row_layout, null);

        TextView title = (TextView) vi.findViewById(R.id.title); // title
        TextView content = (TextView) vi.findViewById(R.id.content); // content
        TextView date = (TextView) vi.findViewById(R.id.date); // date
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image); // thumb image

        HashMap<String, String> article = new HashMap<String, String>();
        article = data.get(position);

        String strTitle = article.get(ArticlesProvider.TITLE);
        String strContent = article.get(ArticlesProvider.CONTENT);
        String strDate = article.get(ArticlesProvider.DATE);
        String strIcon = article.get(ArticlesProvider.ICON);


        // Setting all values in listview
        if (!strTitle.equals("")) {
            title.setText(strTitle);
        }
        if (!strContent.equals("")) {
            content.setText(strContent.substring(0, 40)+"........");
        }
        if (!strDate.equals("")) {
            date.setText(strDate);
        }
        if (!strIcon.equals("")) {
            imageLoader.DisplayImage(strIcon, thumb_image);
        }
        return vi;
    }
}
