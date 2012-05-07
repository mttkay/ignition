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

import android.app.Activity;
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
import android.view.ViewGroup.LayoutParams;
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

    private String imageUrl;
    private boolean autoLoad, isLoaded;

    private ViewSwitcher switcher;
    private Drawable progressDrawable, errorDrawable;

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
     * Since {@link RemoteImageView} and the ViewSwitcher it is contained in share the same view
     * IDs, Android's own findViewById may cause problems by not returning the view you expect.
     * Using this helper, you're guaranteed to always retrieve the {@link RemoteImageView} and not
     * the view switcher. You can still get a reference to the switcher via getParent().
     * 
     * @param activity
     *            the current activity
     * @param id
     *            the {@link RemoteImageView}s view ID
     * @return the {@link RemoteImageView}
     */
    public static RemoteImageView findViewById(Activity activity, int id) {
        View view = activity.findViewById(id);
        if (view instanceof ViewSwitcher) {
            view = ((ViewSwitcher) view).getChildAt(1);
        }
        return (RemoteImageView) view;
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

        switcher = new ViewSwitcher(context);
        // this is a bit of a smell, since we end up having two views in the same view
        // tree that share an ID, but in my tests I found that findViewById always returned
        // the RemoteImageView, which is what a user would expect, so let's keep this as
        // a curious but useful hack for now ;-)
        switcher.setId(getId());
    }

    private View buildProgressSpinnerView(Context context) {
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

        ViewSwitcher.LayoutParams lp = new ViewSwitcher.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        loadingSpinner.setLayoutParams(lp);

        return loadingSpinner;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!(getParent() instanceof ViewSwitcher)) {
            setupViewSwitcher();
        }
    }

    /**
     * This is called automatically from onLayout, so do not call manually unless you know what
     * you're doing!
     */
    public void setupViewSwitcher() {
        // "migrate" this view's LPs to the new parent
        switcher.setLayoutParams(getLayoutParams());

        // swap with view with a ViewSwitcher and re-insert itself under it
        ViewGroup parent = (ViewGroup) this.getParent();
        int selfIndex = parent.indexOfChild(this);
        parent.removeView(this);
        parent.addView(switcher, selfIndex);

        View progressSpinner = buildProgressSpinnerView(getContext());
        switcher.addView(progressSpinner);

        ViewSwitcher.LayoutParams newParams = new ViewSwitcher.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        switcher.addView(this, newParams);

        if (autoLoad && imageUrl != null) {
            loadImage();
        } else {
            // if we don't have anything to load yet, don't show the progress element
            switcher.setDisplayedChild(1);
        }
    }

    /**
     * Use this method to trigger the image download if you had previously set autoLoad to false.
     */
    public void loadImage() {
        if (imageUrl == null) {
            throw new IllegalStateException(
                    "image URL is null; did you forget to set it for this view?");
        }
        switcher.setDisplayedChild(0);
        imageLoader.loadImage(imageUrl, this, new DefaultImageLoaderHandler());
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
        setImageDrawable(getContext().getResources().getDrawable(imageResourceId));
        switcher.setDisplayedChild(1);
    }

    public void reset() {
        switcher.reset();
        switcher.setDisplayedChild(0);
    }

    private class DefaultImageLoaderHandler extends RemoteImageLoaderHandler {

        public DefaultImageLoaderHandler() {
            super(RemoteImageView.this, imageUrl, errorDrawable);
        }

        @Override
        protected boolean handleImageLoaded(Bitmap bitmap, Message msg) {
            boolean wasUpdated = super.handleImageLoaded(bitmap, msg);
            if (wasUpdated) {
                isLoaded = true;
                switcher.setDisplayedChild(1);
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

    /**
     * The progress bar that is shown while the image is loaded.
     * 
     * @return the {@link ProgressBar}
     */
    public View getProgressBar() {
        return switcher.getChildAt(0);
    }
}
