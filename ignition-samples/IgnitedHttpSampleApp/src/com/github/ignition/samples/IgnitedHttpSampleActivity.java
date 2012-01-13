package com.github.ignition.samples;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ignition.core.tasks.IgnitedAsyncTask;
import com.github.ignition.support.http.IgnitedHttp;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.github.ignition.support.http.cache.CachedHttpResponse;
import com.github.ignition.support.http.cache.HttpResponseCache;

public class IgnitedHttpSampleActivity extends Activity {

    private ImageView imageView;
    private TextView statusText;
    
    private IgnitedHttp http;    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        imageView = (ImageView) findViewById(R.id.image);
        statusText = (TextView) findViewById(R.id.status);
        http = new IgnitedHttp(this);
        http.enableResponseCache(this, 5, 30, 1, HttpResponseCache.DISK_CACHE_INTERNAL);        
    }

    public void onDownloadButtonClicked(View button) {
        HttpTask task = new HttpTask(http);
        task.connect(this);
        task.execute();
    }
    
    public void updateStatusText(String message) {
        statusText.setText(message);
    }

    public void updateCacheStatus(String url, boolean cachedResponse) {
        EditText memoryCacheText = (EditText) findViewById(R.id.text_cache_memory);
        memoryCacheText.setText("");
        EditText diskCacheText = (EditText) findViewById(R.id.text_cache_disk);
        diskCacheText.setText("");
        
        HttpResponseCache cache = http.getResponseCache();
        
        for (String key : cache.keySet()) {
            if (cache.containsKeyInMemory(key)) {
                memoryCacheText.append(key + "\n");
            } else if (cache.containsKey(key)) {
                diskCacheText.append(key + "\n");
            }
        }
        
        if (cachedResponse) {
            if (cache.containsKeyInMemory(url)) {
                statusText.setText("Image served from in-memory cache");
            } else if (cache.containsKey(url)) {
                statusText.setText("Image served from disk cache");
            }
        } else {
            statusText.setText("Image downloaded from Web");
        }
        
    }
    
    public void updateImageView(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
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
            context.updateStatusText("Downloading image...");
        }

        @Override
        protected IgnitedHttpResponse run(Void... params) throws Exception {
            IgnitedHttpResponse response = http.get(url, true).retries(3).send();
            return response;
        }

        @Override
        protected void onError(IgnitedHttpSampleActivity context, Exception error) {
            super.onError(context, error); // prints a stack trace

            context.updateStatusText("Download failed!");
            
            Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onSuccess(IgnitedHttpSampleActivity context, IgnitedHttpResponse response) {
            //context.updateImageView(BitmapFactory.decodeStream(response.getResponseBody()));
            
            boolean cachedResponse = response instanceof CachedHttpResponse;
            
            context.updateCacheStatus(url, cachedResponse);
        }
    }
}