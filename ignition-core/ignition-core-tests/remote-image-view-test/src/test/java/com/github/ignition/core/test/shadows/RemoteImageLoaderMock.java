package com.github.ignition.core.test.shadows;

import java.util.LinkedList;
import java.util.List;

import android.widget.ImageView;

import com.github.ignition.core.widgets.RemoteImageView;
import com.github.ignition.support.images.remote.RemoteImageLoader;
import com.github.ignition.support.images.remote.RemoteImageLoaderHandler;
import com.xtremelabs.robolectric.Robolectric;

public class RemoteImageLoaderMock extends RemoteImageLoader {

    private List<Integer> loadedImages = new LinkedList<Integer>();

    public RemoteImageLoaderMock() {
        super(Robolectric.application, false);
    }

    @Override
    public void loadImage(String imageUrl, ImageView imageView) {
        loadedImages.add(imageView.getId());
    }

    @Override
    public void loadImage(String imageUrl, ImageView imageView, RemoteImageLoaderHandler handler) {
        loadedImages.add(imageView.getId());
    }

    public boolean isLoadImageCalled(RemoteImageView view) {
        return loadedImages.contains(view.getId());
    }
}
