package com.github.ignition.samples.core;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.github.ignition.core.tasks.IgnitedAsyncTask;

public class EndlessListActivity extends ListActivity implements OnScrollListener {

    private static final int PAGE_SIZE = 10;

    private InfinityAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.endless_lists_sample);

        adapter = new InfinityAdapter(this);
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);

        loadNextPage();
    }

    private void loadNextPage() {
        adapter.setIsLoadingData(true);
        IgnitedAsyncTask<EndlessListActivity, Void, Void, Void> task = new IgnitedAsyncTask<EndlessListActivity, Void, Void, Void>() {
            @Override
            public Void run(Void... params) throws Exception {
                SystemClock.sleep(1000);
                int offset = adapter.getCount();
                for (int i = 1; i <= PAGE_SIZE; i++) {
                    adapter.getData().add(i + offset);
                }
                return null;
            }

            @Override
            public boolean onTaskCompleted(Void result) {
                adapter.setIsLoadingData(false);
                adapter.notifyDataSetChanged();
                return true;
            }
        };
        task.execute();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (adapter.shouldRequestNextPage(firstVisibleItem, visibleItemCount, totalItemCount)) {
            loadNextPage();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}