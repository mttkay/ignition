package com.github.ignition.samples.remoteimageview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.github.ignition.core.widgets.RemoteImageView;

public class RemoteImageViewActivity extends Activity {

    private RemoteImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        imageView = (RemoteImageView) findViewById(R.id.image5);
    }

    public void loadImage(View button) {
        imageView.loadImage();
    }
}