package com.github.ignition.samples.core;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SampleListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_list);

        ArrayAdapter<Sample> adapter = new ArrayAdapter<Sample>(this,
                android.R.layout.simple_list_item_1);
        adapter.add(new Sample("IgnitedAsyncTask", IgnitedAsyncTaskActivity.class));
        adapter.add(new Sample("EndlessListAdapter", EndlessListActivity.class));
        adapter.add(new Sample("RemoteImageView", RemoteImageViewActivity.class));
        adapter.add(new Sample("RemoteImageGalleryAdapter", RemoteImageGalleryActivity.class));
        adapter.add(new Sample("ScrollingTextView", ScrollingTextViewActivity.class));

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Sample sample = (Sample) getListAdapter().getItem(position);
        Intent intent = new Intent(this, sample.activityClass);
        startActivity(intent);
    }

    private static final class Sample {
        public Sample(String title, Class<?> activityClass) {
            this.title = title;
            this.activityClass = activityClass;
        }

        private String title;
        private Class<?> activityClass;

        @Override
        public String toString() {
            return title;
        }
    }
}
