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

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.github.ignition.support.cache.ImageCache;

/**
 * Realizes a background image loader that downloads an image from a URL, optionally backed by a
 * two-level FIFO cache. If the image to be loaded is present in the cache, it is set immediately on
 * the given view. Otherwise, a thread from a thread pool will be used to download the image in the
 * background and set the image on the view as soon as it completes.
 * 
 * @author Matthias Kaeppler
 */
public class RemoteImageLoader {

    // the default thread pool size
    private static final int DEFAULT_POOL_SIZE = 3;
    // expire images after a day
    // TODO: this currently only affects the in-memory cache, so it's quite pointless
    private static final int DEFAULT_TTL_MINUTES = 24 * 60;
    private static final int DEFAULT_NUM_RETRIES = 3;
    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private ThreadPoolExecutor executor;
    private ImageCache imageCache;
    private int numRetries = DEFAULT_NUM_RETRIES;
    private int defaultBufferSize = DEFAULT_BUFFER_SIZE;
    private long expirationInMinutes = DEFAULT_TTL_MINUTES;

    private Drawable defaultDummyDrawable, errorDrawable;

    public RemoteImageLoader(Context context) {
        this(context, true);
    }

    /**
     * Creates a new ImageLoader that is backed by an {@link ImageCache}. The cache will by default
     * cache to the device's external storage, and expire images after 1 day. You can set useCache
     * to false and then supply your own image cache instance via {@link #setImageCache(ImageCache)}
     * , or fine-tune the default one through {@link #getImageCache()}.
     * 
     * @param context
     *            the current context
     * @param createCache
     *            whether to create a default {@link ImageCache} used for caching
     */
    public RemoteImageLoader(Context context, boolean createCache) {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
        if (createCache) {
            imageCache = new ImageCache(25, expirationInMinutes, DEFAULT_POOL_SIZE);
            imageCache.enableDiskCache(context.getApplicationContext(),
                    ImageCache.DISK_CACHE_SDCARD);
        }
        errorDrawable = context.getResources().getDrawable(android.R.drawable.ic_dialog_alert);
    }

    /**
     * @param numThreads
     *            the maximum number of threads that will be started to download images in parallel
     */
    public void setThreadPoolSize(int numThreads) {
        executor.setMaximumPoolSize(numThreads);
    }

    /**
     * @param numAttempts
     *            how often the image loader should retry the image download if network connection
     *            fails
     */
    public void setMaxDownloadAttempts(int numAttempts) {
        numRetries = numAttempts;
    }

    /**
     * If the server you're loading images from does not report file sizes via the Content-Length
     * header, then you can use this method to tell the downloader how much space it should allocate
     * by default when downloading an image into memory.
     * 
     * @param defaultBufferSize
     *            how big the buffer should be into which the image file is read. This should be big
     *            enough to hold the largest image you expect to download
     */
    public void setDefaultBufferSize(int defaultBufferSize) {
        this.defaultBufferSize = defaultBufferSize;
    }

    public void setDownloadInProgressDrawable(Drawable drawable) {
        this.defaultDummyDrawable = drawable;
    }

    public void setDownloadFailedDrawable(Drawable drawable) {
        this.errorDrawable = drawable;
    }

    public void setImageCache(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    /**
     * Clears the image cache, if it's used. A good candidate for calling in
     * {@link android.app.Application#onLowMemory()}.
     */
    public void clearImageCache() {
        if (imageCache != null) {
            imageCache.clear();
        }
    }

    /**
     * Returns the image cache backing this image loader.
     * 
     * @return the {@link ImageCache}
     */
    public ImageCache getImageCache() {
        return imageCache;
    }

    /**
     * Triggers the image loader for the given image and view. The image loading will be performed
     * concurrently to the UI main thread, using a fixed size thread pool. The loaded image will be
     * posted back to the given ImageView upon completion. This method will the default
     * {@link RemoteImageLoaderHandler} to process the bitmap after downloading it.
     * 
     * @param imageUrl
     *            the URL of the image to download
     * @param imageView
     *            the ImageView which should be updated with the new image
     */
    public void loadImage(String imageUrl, ImageView imageView) {
        loadImage(imageUrl, imageView, defaultDummyDrawable, new RemoteImageLoaderHandler(
                imageView, imageUrl, errorDrawable));
    }

    /**
     * Triggers the image loader for the given image and view. The image loading will be performed
     * concurrently to the UI main thread, using a fixed size thread pool. The loaded image will be
     * posted back to the given ImageView upon completion. This method will the default
     * {@link RemoteImageLoaderHandler} to process the bitmap after downloading it.
     *
     * @param imageUrl
     *            the URL of the image to download
     * @param imageView
     *            the ImageView which should be updated with the new image
     * @param dummyDrawable
     *            the Drawable to be shown while the image is being downloaded.
     */
    public void loadImage(String imageUrl, ImageView imageView, Drawable dummyDrawable) {
        loadImage(imageUrl, imageView, dummyDrawable, new RemoteImageLoaderHandler(
                imageView, imageUrl, errorDrawable));
    }

    /**
     * Triggers the image loader for the given image and view. The image loading will be performed
     * concurrently to the UI main thread, using a fixed size thread pool. The loaded image will be
     * posted back to the given ImageView upon completion.
     * 
     * @param imageUrl
     *            the URL of the image to download
     * @param imageView
     *            the ImageView which should be updated with the new image
     * @param handler
     *            the handler that will process the bitmap after completion
     */
    public void loadImage(String imageUrl, ImageView imageView, RemoteImageLoaderHandler handler) {
        loadImage(imageUrl, imageView, defaultDummyDrawable, handler);
    }

    /**
     * Triggers the image loader for the given image and view. The image loading will be performed
     * concurrently to the UI main thread, using a fixed size thread pool. The loaded image will be
     * posted back to the given ImageView upon completion. While waiting, the dummyDrawable is
     * shown.
     * 
     * @param imageUrl
     *            the URL of the image to download
     * @param imageView
     *            the ImageView which should be updated with the new image
     * @param dummyDrawable
     *            the Drawable to be shown while the image is being downloaded.
     * @param handler
     *            the handler that will process the bitmap after completion
     */
    public void loadImage(String imageUrl, ImageView imageView, Drawable dummyDrawable,
            RemoteImageLoaderHandler handler) {
        if (imageView != null) {
            if (imageUrl == null) {
                // In a ListView views are reused, so we must be sure to remove the tag that could
                // have been set to the ImageView to prevent that the wrong image is set.
                imageView.setTag(null);
                if (dummyDrawable != null) {
                    imageView.setImageDrawable(dummyDrawable);
                }
                return;
            }
            Object oldImageUrl = imageView.getTag();
            if (imageUrl.equals(oldImageUrl)) {
                // nothing to do
                return;
            } else {
                if (dummyDrawable != null) {
                    // Set the dummy image while waiting for the actual image to be downloaded.
                    imageView.setImageDrawable(dummyDrawable);
                }
                imageView.setTag(imageUrl);
            }
        }

        if (imageCache != null && imageCache.containsKeyInMemory(imageUrl)) {
            // do not go through message passing, handle directly instead
            handler.handleImageLoaded(imageCache.getBitmap(imageUrl), null);
        } else {
            executor.execute(new RemoteImageLoaderJob(imageUrl, handler, imageCache, numRetries,
                    defaultBufferSize));
        }
    }
}
