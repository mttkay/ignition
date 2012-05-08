package com.github.ignition.samples.core;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.ignition.core.widgets.RemoteImageView;

public class RemoteImageViewActivity extends Activity {

    private RemoteImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_image_view_sample);

        // normal find by ID (see JavaDoc of RemoteImageView why this may be problematic)
        imageView = (RemoteImageView) findViewById(R.id.image5);

        // fail-safe find by ID (this is guaranteed to work)
        RemoteImageView riv = (RemoteImageView) findViewById(R.id.image1);
        Log.i(getClass().getSimpleName(), "custom findViewById: "
                + riv.getClass().getCanonicalName());
    }

    public void loadImage(View button) {
        imageView.loadImage();
    }
}