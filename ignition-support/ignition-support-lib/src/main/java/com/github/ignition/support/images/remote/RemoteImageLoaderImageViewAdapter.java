package com.github.ignition.support.images.remote;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class RemoteImageLoaderImageViewAdapter extends
        RemoteImageLoaderHandler.RemoteImageLoaderViewAdapter {

    public RemoteImageLoaderImageViewAdapter(String imageUrl, View view, Drawable errorDrawable) {
        super(imageUrl, view, errorDrawable);
    }

    @Override
    protected void onImageLoadedFailed() {
        ((ImageView) view).setImageDrawable(errorDrawable);
    }

    @Override
    protected void onImageLoadedSuccess(Bitmap bitmap) {
        Bitmap processedBitmap = processBitmap(bitmap);
        ((ImageView) view).setImageBitmap(processedBitmap);
    }

    @Override
    public void setDummyDrawableForView(Drawable drawable) {
        ((ImageView) view).setImageDrawable(drawable);
    }

    @Override
    public ImageView getView() {
        return (ImageView) view;
    }
}
