/* Copyright (c) 2009-2012 Matthias Kaeppler
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

package com.github.ignition.core.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import com.github.ignition.core.Ignition;
import com.github.ignition.core.R;
import com.github.ignition.support.images.remote.RemoteImageLoader;
import com.github.ignition.support.images.remote.RemoteImageLoaderHandler;

/**
 * An {@link ImageView} that fetches its image off the web from the supplied URL. While the image is
 * being downloaded, a progress indicator will be shown. The following attributes are supported:
 * <ul>
 * <li>android:src (Drawable) -- The default/placeholder image that is shown if no image can be
 * downloaded, or before the image download starts (see {@link android.R.attr#src})
 * <li>android:indeterminateDrawable (Drawable) -- The progress drawable to use while the image is
 * being downloaded (see {@link android.R.attr#indeterminateDrawable})</li>
 * <li>ignition:imageUrl (String) -- The URL at which the image is found online</li>
 * <li>ignition:autoLoad (Boolean) -- Whether the download should start immediately after view
 * inflation</li>
 * <li>ignition:errorDrawable (Drawable) -- The drawable to display if the image download fails</li>
 * </ul>
 * Being an ImageView itself, all other attributes of Android's own ImageView are supported, too.
 * <p>
 * This functionality is realized by the view dynamically re-attaching itself in the Activity's view
 * tree under a {@link ViewSwitcher}. You can retrieve a reference to the switcher by a call to
 * {@link View#getParent()}. In order for layouting to work properly in Android's
 * {@link RelativeLayout}, the view itself and the switcher (its parent view) share the same view
 * IDs (a circumstance that's not optimal but allowed by Android). If you find that calls to
 * findViewById return the switcher instead of the view itself, please use the provided
 * {@link #findViewById(Activity, int))} helper method instead, which guarantees to return instances
 * of this class.
 * </p>
 * 
 * @author Matthias Kaeppler
 */
public class RemoteImageView extends ImageView {

    public static final int DEFAULT_ERROR_DRAWABLE_RES_ID = android.R.drawable.ic_dialog_alert;

    private static final String ATTR_AUTO_LOAD = "autoLoad";
    private static final String ATTR_IMAGE_URL = "imageUrl";
    private static final String ATTR_ERROR_DRAWABLE = "errorDrawable";

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_LOADED = 1;
    private static final int STATE_LOADING = 2;

    private int state = STATE_DEFAULT;
    private String imageUrl;
    private boolean autoLoad;

    private ViewGroup progressViewContainer;
    private Drawable progressDrawable, errorDrawable;

    private RemoteImageLoader imageLoader;
    private static RemoteImageLoader sharedImageLoader;
    private RemoteImageViewListener listener;

    /**
     * Use this method to inject an image loader that will be shared across all instances of this
     * class. If the shared reference is null, a new {@link RemoteImageLoader} will be instantiated
     * for every instance of this class.
     * 
     * @param imageLoader
     *            the shared image loader
     */
    public static void setSharedImageLoader(RemoteImageLoader imageLoader) {
        sharedImageLoader = imageLoader;
    }

    /**
     * @param context
     *            the view's current context
     * @param imageUrl
     *            the URL of the image to download and show
     * @param autoLoad
     *            Whether the download should start immediately after creating the view. If set to
     *            false, use {@link #loadImage()} to manually trigger the image download.
     */
    public RemoteImageView(Context context, String imageUrl, boolean autoLoad) {
        super(context);
        initialize(context, imageUrl, null, null, autoLoad, null);
    }

    /**
     * @param context
     *            the view's current context
     * @param imageUrl
     *            the URL of the image to download and show
     * @param progressDrawable
     *            the drawable to be used for the {@link ProgressBar} which is displayed while the
     *            image is loading
     * @param errorDrawable
     *            the drawable to be used if a download error occurs
     * @param autoLoad
     *            Whether the download should start immediately after creating the view. If set to
     *            false, use {@link #loadImage()} to manually trigger the image download.
     */
    public RemoteImageView(Context context, String imageUrl, Drawable progressDrawable,
            Drawable errorDrawable, boolean autoLoad) {
        super(context);
        initialize(context, imageUrl, progressDrawable, errorDrawable, autoLoad, null);
    }

    public RemoteImageView(Context context, AttributeSet attributes) {
        super(context, attributes);

        // Read all Android specific view attributes into a typed array first.
        // These are attributes that are specific to RemoteImageView, but which are not in the
        // ignition XML namespace.
        TypedArray imageViewAttrs = context.getTheme().obtainStyledAttributes(attributes,
                R.styleable.RemoteImageView, 0, 0);
        int progressDrawableId = imageViewAttrs.getResourceId(
                R.styleable.RemoteImageView_android_indeterminateDrawable, 0);
        imageViewAttrs.recycle();

        int errorDrawableId = attributes.getAttributeResourceValue(Ignition.XMLNS,
                ATTR_ERROR_DRAWABLE, DEFAULT_ERROR_DRAWABLE_RES_ID);
        Drawable errorDrawable = context.getResources().getDrawable(errorDrawableId);

        Drawable progressDrawable = null;
        if (progressDrawableId > 0) {
            progressDrawable = context.getResources().getDrawable(progressDrawableId);
        }

        String imageUrl = attributes.getAttributeValue(Ignition.XMLNS, ATTR_IMAGE_URL);
        boolean autoLoad = attributes
                .getAttributeBooleanValue(Ignition.XMLNS, ATTR_AUTO_LOAD, true);

        initialize(context, imageUrl, progressDrawable, errorDrawable, autoLoad, attributes);
    }

    private void initialize(Context context, String imageUrl, Drawable progressDrawable,
            Drawable errorDrawable, boolean autoLoad, AttributeSet attributes) {
        this.imageUrl = imageUrl;
        this.autoLoad = autoLoad;
        this.progressDrawable = progressDrawable;
        this.errorDrawable = errorDrawable;
        if (sharedImageLoader == null) {
            this.imageLoader = new RemoteImageLoader(context);
        } else {
            this.imageLoader = sharedImageLoader;
        }

        progressViewContainer = new FrameLayout(getContext());
        progressViewContainer.addView(buildProgressSpinnerView(getContext()));

        if (autoLoad) {
            loadImage();
        } else {
            showProgressView(false);
        }
    }

    protected View buildProgressSpinnerView(Context context) {
        ProgressBar loadingSpinner = new ProgressBar(context);
        loadingSpinner.setIndeterminate(true);
        if (this.progressDrawable == null) {
            this.progressDrawable = loadingSpinner.getIndeterminateDrawable();
        } else {
            loadingSpinner.setIndeterminateDrawable(progressDrawable);
            if (progressDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) progressDrawable).start();
            }
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                progressDrawable.getIntrinsicWidth(), progressDrawable.getIntrinsicHeight(),
                Gravity.CENTER);
        loadingSpinner.setLayoutParams(lp);

        return loadingSpinner;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // insert the progress view and its container as a sibling to this image view
        if (progressViewContainer.getParent() == null) {
            // the container for the progress view must behave identically to this view during
            // layouting, otherwise e.g. relative positioning via IDs will break
            progressViewContainer.setLayoutParams(getLayoutParams());

            ViewGroup parent = (ViewGroup) getParent();
            int index = parent.indexOfChild(this);
            parent.addView(progressViewContainer, index + 1);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Use this method to trigger the image download if you had previously set autoLoad to false.
     */
    public void loadImage() {
        if (state != STATE_LOADING) {
            if (imageUrl == null) {
                throw new IllegalStateException(
                        "image URL is null; did you forget to set it for this view?");
            }
            showProgressView(true);
            imageLoader.loadImage(imageUrl, this, new DefaultImageLoaderHandler());
        }
    }

    public boolean isLoaded() {
        return state == STATE_LOADED;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Often you have resources which usually have an image, but some don't. For these cases, use
     * this method to supply a placeholder drawable which will be loaded instead of a web image.
     * 
     * @param imageResourceId
     *            the resource of the placeholder image drawable
     */
    public void setNoImageDrawable(int imageResourceId) {
        setImageDrawable(getContext().getResources().getDrawable(imageResourceId));
        showProgressView(false);
    }

    /**
     * Reset to view to its initial state, i.e. hide the progress item and show the image.
     */
    public void reset() {
        showProgressView(false);
    }

    private void showProgressView(boolean show) {
        if (show) {
            state = STATE_LOADING;
            progressViewContainer.setVisibility(View.VISIBLE);
            setVisibility(View.INVISIBLE);
        } else {
            state = STATE_DEFAULT;
            progressViewContainer.setVisibility(View.INVISIBLE);
            setVisibility(View.VISIBLE);
        }
    }

    private class DefaultImageLoaderHandler extends RemoteImageLoaderHandler {

        public DefaultImageLoaderHandler() {
            super(RemoteImageView.this, imageUrl, errorDrawable);
        }

        @Override
        protected boolean handleImageLoaded(Bitmap bitmap, Message msg) {
            boolean wasUpdated = super.handleImageLoaded(bitmap, msg);
            if (wasUpdated) {
                state = STATE_LOADED;
                if (listener != null) {
                    listener.onImageLoaded(bitmap);
                }
                showProgressView(false);
            }
            return wasUpdated;
        }
    }

    /**
     * Returns the URL of the image to show. Corresponds to the view attribute ignition:imageUrl.
     * 
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Whether or not the image should be downloaded immediately after view inflation. Corresponds
     * to the view attribute ignition:autoLoad (default: true).
     * 
     * @return true if auto downloading of the image is enabled
     */
    public boolean isAutoLoad() {
        return autoLoad;
    }

    /**
     * The drawable that should be used to indicate progress while downloading the image.
     * Corresponds to the view attribute android:indeterminateDrawable. If left blank, the
     * platform's standard indeterminate progress drawable will be used.
     * 
     * @return the progress drawable
     */
    public Drawable getProgressDrawable() {
        return progressDrawable;
    }

    /**
     * The drawable that will be shown when the image download fails. Corresponds to the view
     * attribute ignition:errorDrawable. If left blank, a stock alert icon from the Android platform
     * will be used.
     * 
     * @return the error drawable
     */
    public Drawable getErrorDrawable() {
        return errorDrawable;
    }

    /**
     * The progress view that is shown while the image is loaded.
     * 
     * @return the progress view, default is a {@link ProgressBar}
     */
    public View getProgressView() {
        return progressViewContainer.getChildAt(0);
    }

    public static interface RemoteImageViewListener {
        public void onImageLoaded(Bitmap bm);
    }

    /**
     * Use this method to set a listener for events raised by the remote image view such as image
     * loaded.
     * 
     * @param listener
     *            an implementation of the {@link RemoteImageViewListener} interface
     */
    public void setRemoteImageViewListener(RemoteImageViewListener listener) {
        this.listener = listener;
    }
}
