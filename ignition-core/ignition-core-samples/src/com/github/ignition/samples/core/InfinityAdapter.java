package com.github.ignition.samples.core;

import android.app.ListActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ignition.core.adapters.EndlessListAdapter;

public class InfinityAdapter extends EndlessListAdapter<Integer> {

    public InfinityAdapter(ListActivity activity) {
        super(activity, R.layout.loading_item);
    }

    @Override
    protected View doGetView(int position, View convertView, ViewGroup parentView) {
        int item = getItem(position);
        TextView textView = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getListView().getContext());
            textView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1,
                    getListView(), false);
        } else {
            textView = (TextView) convertView;
        }
        textView.setText("Item " + String.valueOf(item));
        return textView;
    }

}
