package com.github.ignition.samples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.github.ignition.core.tasks.IgnitedAsyncTask;
import com.github.ignition.support.http.IgnitedHttp;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.github.ignition.support.http.cache.CachedHttpResponse;
import com.github.ignition.support.http.cache.HttpResponseCache;

import java.io.File;
import java.util.List;

public class IgnitedHttpSampleActivity extends Activity {

    private TextView statusText;
    private CheckBox useCache;

    private IgnitedHttp http;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        statusText = (TextView) findViewById(R.id.status);
        useCache = (CheckBox) findViewById(R.id.chk_use_cache);
        useCache.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableCache(isChecked);
                updateCacheStatus();
            }
        });

        http = new IgnitedHttp(this);
        enableCache(useCache.isChecked());
    }

    private void enableCache(boolean isChecked) {
        if (isChecked) {
            http.enableResponseCache(IgnitedHttpSampleActivity.this, 3, 30, 1,
                    HttpResponseCache.DISK_CACHE_INTERNAL);
        } else {
            http.disableResponseCache(true);
        }
    }

    public void onDownloadButtonClicked(View button) {
        HttpTask task = new HttpTask(http);
        task.connect(this);
        task.execute();
    }

    public void onClearAll(View button) {
        http.getResponseCache().clear();
        updateCacheStatus();
    }

    public void onClearMemory(View button) {
        http.getResponseCache().clear(false);
        updateCacheStatus();
    }

    public void updateStatusText(String message) {
        statusText.setText(message);
    }

    public void updateStatusText(String url, boolean cachedResponse) {
        if (cachedResponse) {
            statusText.setText("File served from cache");
        } else {
            statusText.setText("File downloaded from Web");
        }
    }

    public void updateCacheStatus() {
        EditText memoryCacheText = (EditText) findViewById(R.id.text_cache_memory);
        memoryCacheText.setText("");
        EditText diskCacheText = (EditText) findViewById(R.id.text_cache_disk);
        diskCacheText.setText("");

        HttpResponseCache cache = http.getResponseCache();
        if (cache != null) {
            for (String key : cache.keySet()) {
                if (cache.containsKeyInMemory(key)) {
                    memoryCacheText.append(cache.getFileNameForKey(key) + "\n");
                }
            }
            List<File> cachedFiles = cache.getCachedFiles();
            for (File file : cachedFiles) {
                diskCacheText.append(file.getName() + "\n");
            }
        }
    }

    public static final class HttpTask extends
            IgnitedAsyncTask<IgnitedHttpSampleActivity, Void, Void, IgnitedHttpResponse> {

        private IgnitedHttp http;

        private String url = "http://developer.android.com/images/home/android-design.png";

        public HttpTask(IgnitedHttp http) {
            this.http = http;
        }

        @Override
        protected void onStart(IgnitedHttpSampleActivity context) {
            context.updateStatusText("Downloading file...");
        }

        @Override
        protected IgnitedHttpResponse run(Void... params) throws Exception {
            return http.get(url, true).retries(3).expecting(200).send();
        }

        @Override
        protected void onError(IgnitedHttpSampleActivity context, Exception error) {
            super.onError(context, error); // prints a stack trace

            context.updateStatusText("Download failed! " + error.getCause().getMessage());

            Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onSuccess(IgnitedHttpSampleActivity context, IgnitedHttpResponse response) {
            boolean cachedResponse = response instanceof CachedHttpResponse;

            context.updateStatusText(url, cachedResponse);
            context.updateCacheStatus();
        }
    }
}