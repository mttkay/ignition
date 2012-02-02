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

package com.github.ignition.core.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;
import com.github.ignition.core.Ignition;
import com.github.ignition.support.images.remote.RemoteImageLoader;
import com.github.ignition.support.images.remote.RemoteImageLoaderHandler;

/**
 * An image view that fetches its image off the web using the supplied URL. While the image is being
 * downloaded, a progress indicator will be shown.
 * 
 * @author Matthias Kaeppler
 */
public class RemoteImageView extends ViewSwitcher {

    public static final int DEFAULT_ERROR_DRAWABLE_RES_ID = android.R.drawable.ic_dialog_alert;
    public static final int DEFAULT_DRAWABLE_RES_ID = android.R.drawable.gallery_thumb;
    
    private static final String ATTR_AUTO_LOAD = "autoLoad";
    private static final String ATTR_IMAGE_URL = "imageUrl";
    private static final String ATTR_DEFAULT_DRAWABLE = "defaultDrawable";
    private static final String ATTR_PROGRESS_DRAWABLE = "progressDrawable";
    private static final String ATTR_ERROR_DRAWABLE = "errorDrawable";

    private String imageUrl;

    private boolean autoLoad, isLoaded;

    private ProgressBar loadingSpinner;

    private ImageView imageView;

    private ScaleType scaleType = ScaleType.CENTER_CROP;

    private Drawable progressDrawable, errorDrawable, defaultDrawable;

    private RemoteImageLoader imageLoader;

    private static RemoteImageLoader sharedImageLoader;

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
        initialize(context, imageUrl, null, null, null, autoLoad);
    }

    /**
     * @param context
     *            the view's current context
     * @param imageUrl
     *            the URL of the image to download and show
     * @param progressDrawable
     *            the drawable to be used for the {@link ProgressBar} which is displayed while the
     *            image is loading
     * @param autoLoad
     *            Whether the download should start immediately after creating the view. If set to
     *            false, use {@link #loadImage()} to manually trigger the image download.
     */
    public RemoteImageView(Context context, String imageUrl, Drawable progressDrawable,
            boolean autoLoad) {
        super(context);
        initialize(context, imageUrl, progressDrawable, null, null, autoLoad);
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
        initialize(context, imageUrl, progressDrawable, errorDrawable, null, autoLoad);
    }

    public RemoteImageView(Context context, AttributeSet attributes) {
        super(context, attributes);
        // TypedArray styles = context.obtainStyledAttributes(attributes,
        // R.styleable.GalleryItem);
        int progressDrawableId = attributes.getAttributeResourceValue(Ignition.XMLNS,
                ATTR_PROGRESS_DRAWABLE, 0);
        int errorDrawableId = attributes.getAttributeResourceValue(Ignition.XMLNS,
                ATTR_ERROR_DRAWABLE, DEFAULT_ERROR_DRAWABLE_RES_ID);
        int defaultDrawableId = attributes.getAttributeResourceValue(Ignition.XMLNS,
                ATTR_DEFAULT_DRAWABLE, DEFAULT_DRAWABLE_RES_ID);

        Drawable defaultDrawable = context.getResources().getDrawable(defaultDrawableId);
        Drawable errorDrawable = context.getResources().getDrawable(errorDrawableId);

        Drawable progressDrawable = null;
        if (progressDrawableId > 0) {
            progressDrawable = context.getResources().getDrawable(progressDrawableId);
        }

        String imageUrl = attributes.getAttributeValue(Ignition.XMLNS, ATTR_IMAGE_URL);
        boolean autoLoad = attributes
                .getAttributeBooleanValue(Ignition.XMLNS, ATTR_AUTO_LOAD, true);

        initialize(context, imageUrl, progressDrawable, errorDrawable, defaultDrawable, autoLoad);
        // styles.recycle();
    }

    private void initialize(Context context, String imageUrl, Drawable progressDrawable,
            Drawable errorDrawable, Drawable defaultDrawable, boolean autoLoad) {
        this.imageUrl = imageUrl;
        this.autoLoad = autoLoad;
        this.progressDrawable = progressDrawable;
        this.errorDrawable = errorDrawable;
        this.defaultDrawable = defaultDrawable;
        if (sharedImageLoader == null) {
            this.imageLoader = new RemoteImageLoader(context);
        } else {
            this.imageLoader = sharedImageLoader;
        }

        // ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
        // 125.0f, preferredItemHeight / 2.0f);
        // anim.setDuration(500L);

        // AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        // anim.setDuration(500L);
        // setInAnimation(anim);

        addLoadingSpinnerView(context);
        addImageView(context);

        if (autoLoad && imageUrl != null) {
            loadImage();
        } else {
            // if we don't have anything to load yet, don't show the progress element
            setDisplayedChild(1);
        }
    }

    private void addLoadingSpinnerView(Context context) {
        loadingSpinner = new ProgressBar(context);
        loadingSpinner.setIndeterminate(true);
        if (this.progressDrawable == null) {
            this.progressDrawable = loadingSpinner.getIndeterminateDrawable();
        } else {
            loadingSpinner.setIndeterminateDrawable(progressDrawable);
            if (progressDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) progressDrawable).start();
            }
        }

        LayoutParams lp = new LayoutParams(progressDrawable.getIntrinsicWidth(),
                progressDrawable.getIntrinsicHeight());
        lp.gravity = Gravity.CENTER;

        addView(loadingSpinner, 0, lp);
    }

    private void addImageView(Context context) {
        imageView = new ImageView(context);
        imageView.setScaleType(scaleType);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        if (defaultDrawable != null) {
            imageView.setImageDrawable(defaultDrawable);
        }
        addView(imageView, 1, lp);
    }

    /**
     * Use this method to trigger the image download if you had previously set autoLoad to false.
     */
    public void loadImage() {
        if (imageUrl == null) {
            throw new IllegalStateException(
                    "image URL is null; did you forget to set it for this view?");
        }
        setDisplayedChild(0);
        imageLoader.loadImage(imageUrl, imageView, new DefaultImageLoaderHandler());
    }

    public boolean isLoaded() {
        return isLoaded;
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
        imageView.setImageDrawable(getContext().getResources().getDrawable(imageResourceId));
        setDisplayedChild(1);
    }

    @Override
    public void reset() {
        super.reset();

        this.setDisplayedChild(0);
    }

    private class DefaultImageLoaderHandler extends RemoteImageLoaderHandler {

        public DefaultImageLoaderHandler() {
            super(imageView, imageUrl, errorDrawable);
        }

        @Override
        protected boolean handleImageLoaded(Bitmap bitmap, Message msg) {
            boolean wasUpdated = super.handleImageLoaded(bitmap, msg);
            if (wasUpdated) {
                isLoaded = true;
                setDisplayedChild(1);
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
     * The drawable that is shown in place of the downloaded image. It will be replaces once the
     * image has been downloaded. This can be used to display a placeholder image while the download
     * is in progress. Corresponds to the view attribute ignition:defaultDrawable. If left blank, no
     * placeholder image will be used.
     * 
     * @return the placeholder (default) image
     */
    public Drawable getDefaultDrawable() {
        return defaultDrawable;
    }

    /**
     * The drawable that should be used to indicate progress while downloading the image.
     * Corresponds to the view attribute ignition:progressDrawable. If left blank, the platform's
     * standard indeterminate progress drawable will be used.
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
}
