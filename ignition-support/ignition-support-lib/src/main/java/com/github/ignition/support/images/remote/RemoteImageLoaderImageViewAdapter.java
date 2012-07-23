package com.github.ignition.support.images.remote;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

public class RemoteImageLoaderImageViewAdapter extends
        RemoteImageLoaderHandler.RemoteImageLoaderViewAdapter {

    public RemoteImageLoaderImageViewAdapter(String imageUrl, View view, Drawable errorDrawable) {
        super(imageUrl, view, errorDrawable);
    }

    @Override
    public boolean handleImageLoaded(Bitmap bitmap, Message msg) {
        // If this handler is used for loading images in a ListAdapter,
        // the thread will set the image only if it's the right position,
        // otherwise it won't do anything.
        Object viewTag = view.getTag();
        if (imageUrl.equals(viewTag)) {
            if (bitmap == null) {
                if (view != null) {
                    ((ImageView) view).setImageDrawable(errorDrawable);
                }
            } else {
                if (view != null) {
                    ((ImageView) view).setImageBitmap(processBitmap(bitmap));
                }
            }
            // remove the image URL from the view's tag
            view.setTag(null);
            return true;
        }
        return false;
    }

    @Override
    public void setDrawable(Drawable drawable) {
        ((ImageView) view).setImageDrawable(drawable);
    }

    @Override
    public ImageView getView() {
        return (ImageView) view;
    }
}
