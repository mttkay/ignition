package com.github.ignition.support.images.remote;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.widget.TextView;

import com.github.ignition.support.images.remote.RemoteImageLoaderHandler.RemoteImageLoaderViewAdapter;

// TODO: extend this class so that it can handle more than one drawable.
public class RemoteImageLoaderTextViewAdapter extends RemoteImageLoaderViewAdapter {
    private boolean left, top, right, bottom;

    public RemoteImageLoaderTextViewAdapter(String imageUrl, TextView textView,
            Drawable errorDrawable, boolean left, boolean top, boolean right, boolean bottom) {
        super(imageUrl, textView, errorDrawable);
        this.left = left;
        this.top = top;
        this.right = right;
    }

    @Override
    public boolean handleImageLoaded(Bitmap bitmap, Message msg) {
        Object viewTag = view.getTag();
        if (imageUrl.equals(viewTag)) {
            if (bitmap == null) {
                if (view != null) {
                    setCompoundDrawable(errorDrawable);
                }
            } else {
                if (view != null) {
                    Bitmap processedBitmap = processBitmap(bitmap);
                    Drawable drawable = new BitmapDrawable(processedBitmap);
                    setCompoundDrawable(drawable);
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
        setCompoundDrawable(drawable);
    }

    @Override
    public TextView getView() {
        return (TextView) view;
    }

    private void setCompoundDrawable(Drawable drawable) {
        Drawable leftDrawable = left ? drawable : null;
        Drawable topDrawable = top ? drawable : null;
        Drawable rightDrawable = right ? drawable : null;
        Drawable bottomDrawable = bottom ? drawable : null;
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(leftDrawable, topDrawable,
                rightDrawable, bottomDrawable);
    }

}
