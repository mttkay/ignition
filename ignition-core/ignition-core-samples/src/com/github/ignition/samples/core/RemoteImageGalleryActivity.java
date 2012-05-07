package com.github.ignition.samples.core;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView.ScaleType;

import com.github.ignition.core.adapters.RemoteImageGalleryAdapter;
import com.github.ignition.core.widgets.RemoteImageView;
import com.github.ignition.support.IgnitedScreens;
import com.github.ignition.support.images.remote.RemoteImageLoader;

public class RemoteImageGalleryActivity extends Activity {

    private static final String[] imageUrls = {
            "http://farm8.staticflickr.com/7143/6621178411_e52b6ab043.jpg",
            "http://farm8.staticflickr.com/7006/6647956613_a9dcecafeb.jpg",
            "http://farm8.staticflickr.com/7017/6589270313_1236f3546f.jpg" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_image_gallery_sample);

        RemoteImageView.setSharedImageLoader(new RemoteImageLoader(this));

        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new RemoteImageGalleryAdapter(this, Arrays.asList(imageUrls)) {
            @Override
            protected void onGetView(int position, RemoteImageView remoteImageView,
                    ViewGroup remoteImageViewContainer, ViewGroup parent) {
                // just to illustrate how you can adjust aspects of the rendered images here
                remoteImageView.setScaleType(ScaleType.CENTER_CROP);
                remoteImageViewContainer.setBackgroundResource(android.R.drawable.gallery_thumb);
                int padding = IgnitedScreens.dipToPx(parent.getContext(), 15);
                remoteImageViewContainer.setPadding(padding, padding, padding, padding);
            }
        });
    }

}