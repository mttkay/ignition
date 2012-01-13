/* Copyright (c) 2009 Matthias Käppler
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

package com.github.ignition.core.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;

import com.github.ignition.core.widgets.RemoteImageView;

/**
 * Can be used as an adapter for an Android {@link Gallery} view. This adapter loads the images to
 * be shown from the web.
 * 
 * @author Matthias Kaeppler
 */
public class WebGalleryAdapter extends BaseAdapter {

    public static final int NO_DRAWABLE = -1;

    private List<String> imageUrls;

    private Context context;

    private Drawable progressDrawable, errorDrawable;

    public WebGalleryAdapter(Context context) {
        initialize(context, null, null, null);
    }

    /**
     * @param context
     *            the current context
     * @param imageUrls
     *            the set of image URLs which are to be loaded and displayed
     */
    public WebGalleryAdapter(Context context, List<String> imageUrls) {
        initialize(context, imageUrls, null, null);
    }

    /**
     * @param context
     *            the current context
     * @param imageUrls
     *            the set of image URLs which are to be loaded and displayed
     * @param progressDrawableResId
     *            the resource ID of the drawable that will be used for rendering progress
     */
    public WebGalleryAdapter(Context context, List<String> imageUrls, int progressDrawableResId) {
        initialize(context, imageUrls, context.getResources().getDrawable(progressDrawableResId),
                null);
    }

    /**
     * @param context
     *            the current context
     * @param imageUrls
     *            the set of image URLs which are to be loaded and displayed
     * @param progressDrawableResId
     *            the resource ID of the drawable that will be used for rendering progress
     * @param errorDrawableId
     *            the resource ID of the drawable that will be used if a download error occurs
     */
    public WebGalleryAdapter(Context context, List<String> imageUrls, int progressDrawableResId,
            int errorDrawableId) {
        initialize(context, imageUrls, progressDrawableResId == NO_DRAWABLE ? null : context
                .getResources().getDrawable(progressDrawableResId),
                errorDrawableId == NO_DRAWABLE ? null : context.getResources().getDrawable(
                        errorDrawableId));
    }

    private void initialize(Context context, List<String> imageUrls, Drawable progressDrawable,
            Drawable errorDrawable) {
        this.imageUrls = imageUrls;
        this.context = context;
        this.progressDrawable = progressDrawable;
        this.errorDrawable = errorDrawable;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setProgressDrawable(Drawable progressDrawable) {
        this.progressDrawable = progressDrawable;
    }

    public Drawable getProgressDrawable() {
        return progressDrawable;
    }

    // TODO: both convertView and ViewHolder are pointless at the moment, since there's a framework
    // bug which causes views to not be cached in a Gallery widget:
    // http://code.google.com/p/android/issues/detail?id=3376
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String imageUrl = (String) getItem(position);

        ViewHolder viewHolder = null;
        RemoteImageView webImageView = null;

        if (convertView == null) {
            // create the image view
            webImageView = new RemoteImageView(context, null, progressDrawable,
                    errorDrawable, false);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER;
            webImageView.setLayoutParams(lp);

            // create the container layout for the image view
            FrameLayout container = new FrameLayout(context);
            container.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
            container.addView(webImageView, 0);

            convertView = container;

            viewHolder = new ViewHolder();
            viewHolder.webImageView = webImageView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            webImageView = viewHolder.webImageView;
        }

        // calling reset is important to prevent old images from displaying in a recycled view.
        webImageView.reset();

        webImageView.setImageUrl(imageUrl);
        webImageView.loadImage();

        onGetView(position, convertView, parent);

        return convertView;
    }

    protected void onGetView(int position, View convertView, ViewGroup parent) {
        // for extension
    }

    private static final class ViewHolder {
        private RemoteImageView webImageView;
    }
}
