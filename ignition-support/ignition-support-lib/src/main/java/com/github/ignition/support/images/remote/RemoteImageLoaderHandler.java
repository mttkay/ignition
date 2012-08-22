/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ignition.support.images.remote;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RemoteImageLoaderHandler extends Handler {

    public static final int HANDLER_MESSAGE_ID = 0;
    public static final String BITMAP_EXTRA = "ign:extra_bitmap";
    public static final String IMAGE_URL_EXTRA = "ign:extra_image_url";

    @Deprecated
    private ImageView imageView;
    private RemoteImageLoaderViewAdapter remoteImageLoaderViewAdapter;

    public RemoteImageLoaderHandler(ImageView imageView, String imageUrl, Drawable errorDrawable) {
        this.imageView = imageView;
        init(imageView, imageUrl, errorDrawable);
    }

    public RemoteImageLoaderHandler(Looper looper, ImageView imageView, String imageUrl,
            Drawable errorDrawable) {
        super(looper);
        init(imageView, imageUrl, errorDrawable);
    }

    public RemoteImageLoaderHandler(Looper looper, TextView textView, String imageUrl,
            Drawable errorDrawable, boolean[] compoundDrawablesEnabledPositions) {
        super(looper);
        init(textView, imageUrl, errorDrawable, compoundDrawablesEnabledPositions[0],
                compoundDrawablesEnabledPositions[1], compoundDrawablesEnabledPositions[2],
                compoundDrawablesEnabledPositions[3]);
    }

    public RemoteImageLoaderHandler(TextView textView, String imageUrl, Drawable errorDrawable,
            boolean[] compoundDrawablesEnabledPositions) {
        init(textView, imageUrl, errorDrawable, compoundDrawablesEnabledPositions[0],
                compoundDrawablesEnabledPositions[1], compoundDrawablesEnabledPositions[2],
                compoundDrawablesEnabledPositions[3]);
    }

    private void init(TextView textView, String imageUrl, Drawable errorDrawable, boolean left,
            boolean top, boolean right, boolean bottom) {
        this.remoteImageLoaderViewAdapter = new RemoteImageLoaderTextViewAdapter(imageUrl,
                textView, errorDrawable, left, top, right, bottom);
    }

    private void init(ImageView view, String imageUrl, Drawable errorDrawable) {
        this.imageView = view;
        this.remoteImageLoaderViewAdapter = new RemoteImageLoaderImageViewAdapter(imageUrl, view,
                errorDrawable);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == HANDLER_MESSAGE_ID) {
            handleImageLoadedMessage(msg);
        }
    }

    protected final void handleImageLoadedMessage(Message msg) {
        Bundle data = msg.getData();
        Bitmap bitmap = data.getParcelable(BITMAP_EXTRA);
        handleImageLoaded(bitmap, msg);
    }

    /**
     * Override this method if you need custom handler logic. Note that this method can actually be
     * called directly for performance reasons, in which case the message will be null
     * 
     * @param bitmap
     *            the bitmap returned from the image loader
     * @param msg
     *            the handler message; can be null
     * @return true if the view was updated with the new image, false if it was discarded
     */
    protected boolean handleImageLoaded(Bitmap bitmap, Message msg) {
        if (remoteImageLoaderViewAdapter == null) {
            throw new IllegalStateException("A RemoteImageLoaderViewAdapter must be set!");
        }
        return remoteImageLoaderViewAdapter.handleImageLoaded(bitmap, msg);
    }

    public String getImageUrl() {
        return remoteImageLoaderViewAdapter.getImageUrl();
    }

    public void setImageUrl(String imageUrl) {
        remoteImageLoaderViewAdapter.setImageUrl(imageUrl);
    }

    public void setErrorDrawable(Drawable errorDrawable) {
        remoteImageLoaderViewAdapter.setErrorDrawable(errorDrawable);
    }

    public View getView() {
        return remoteImageLoaderViewAdapter.getView();
    }

    public void setView(View view) {
        remoteImageLoaderViewAdapter.setView(view);
    }

    @Deprecated
    public ImageView getImageView() {
        return imageView;
    }

    @Deprecated
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public RemoteImageLoaderViewAdapter getRemoteImageLoaderViewAdapter() {
        return remoteImageLoaderViewAdapter;
    }

    public void setRemoteImageLoaderViewAdapter(
            RemoteImageLoaderViewAdapter remoteImageLoaderViewAdapter) {
        this.remoteImageLoaderViewAdapter = remoteImageLoaderViewAdapter;
    }

    public static abstract class RemoteImageLoaderViewAdapter {
        protected View view;
        protected Drawable errorDrawable;
        protected String imageUrl;

        public RemoteImageLoaderViewAdapter(String imageUrl, View view, Drawable errorDrawable) {
            this.imageUrl = imageUrl;
            this.view = view;
            this.errorDrawable = errorDrawable;
        }

        protected boolean handleImageLoaded(Bitmap bitmap, Message msg) {
            // If this handler is used for loading images in a ListAdapter,
            // the thread will set the image only if it's the right position,
            // otherwise it won't do anything.
            Object viewTag = view.getTag();
            if (imageUrl.equals(viewTag)) {
                if (bitmap == null) {
                    if (view != null) {
                        onImageLoadedFailed();
                    }
                } else {
                    if (view != null) {
                        onImageLoadedSuccess(bitmap);
                    }
                }
                // remove the image URL from the view's tag
                view.setTag(null);
                return true;
            }
            return false;
        }

        protected abstract void onImageLoadedFailed();

        protected abstract void onImageLoadedSuccess(Bitmap bitmap);

        protected abstract void setDummyDrawableForView(Drawable dummyDrawable);

        public void setErrorDrawable(Drawable errorDrawable) {
            this.errorDrawable = errorDrawable;
        }

        public Drawable getErrorDrawable() {
            return errorDrawable;
        }

        public void setView(View view) {
            this.view = view;
        }

        public View getView() {
            return view;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public Bitmap processBitmap(Bitmap bitmap) {
            return bitmap;
        }
    }
}
