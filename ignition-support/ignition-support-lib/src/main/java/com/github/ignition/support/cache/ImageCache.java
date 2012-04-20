/* Copyright (c) 2009 Matthias Kaeppler
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

package com.github.ignition.support.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Implements a cache capable of caching image files. It exposes helper methods to immediately
 * access binary image data as {@link Bitmap} objects.
 * 
 * @author Matthias Kaeppler
 * 
 */
public class ImageCache extends AbstractCache<String, byte[]> {

    protected static class ImageLruCache extends AbstractCache.IgnitedLruCache<String, byte[]> {

        public ImageLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, byte[] value) {
            return value.length;
        }
    }

    public ImageCache(int maxSizeInBytes) {
        super("ImageCache", maxSizeInBytes);
    }

    public ImageCache(Context context) {
        super("ImageCache", getMemoryClass(context, 0.5f));
    }

    public static int getMemoryClass(Context context, float modifier) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return (int) (modifier * am.getMemoryClass());
    }

    public synchronized void removeAllWithPrefix(String urlPrefix) {
        CacheHelper.removeAllWithStringPrefix(this, urlPrefix);
    }

    @Override
    public String getFileNameForKey(String imageUrl) {
        return CacheHelper.getFileNameFromUrl(imageUrl);
    }

    @Override
    protected byte[] readValueFromDisk(File file) throws IOException {
        BufferedInputStream istream = new BufferedInputStream(new FileInputStream(file));
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException("Cannot read files larger than " + Integer.MAX_VALUE + " bytes");
        }

        int imageDataLength = (int) fileSize;

        byte[] imageData = new byte[imageDataLength];
        istream.read(imageData, 0, imageDataLength);
        istream.close();

        return imageData;
    }

    public synchronized Bitmap getBitmap(Object elementKey) {
        byte[] imageData = super.get(elementKey);
        if (imageData == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
    }

    @Override
    protected void writeValueToDisk(File file, byte[] imageData) throws IOException {
        BufferedOutputStream ostream = new BufferedOutputStream(new FileOutputStream(file));

        ostream.write(imageData);

        ostream.close();
    }
}
