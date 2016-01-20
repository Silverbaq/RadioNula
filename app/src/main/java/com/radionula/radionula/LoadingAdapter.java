package com.radionula.radionula;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by silverbaq on 1/20/16.
 */
public class LoadingAdapter extends BaseAdapter {
    Activity activity;

    public LoadingAdapter(Activity activity){
        this.activity= activity;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = activity.getLayoutInflater().inflate(R.layout.adapter_loading, parent);

        TextView textView = (TextView) convertView.findViewById(R.id.adapter_loading_textview);
        Typeface artistFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf");
        textView.setTypeface(artistFont);

        return convertView;
    }
}
