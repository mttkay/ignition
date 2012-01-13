package com.github.ignition.support.cache;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

public class CacheHelper {

    public static String getFileNameFromUrl(String url) {
        // replace all special URI characters with a single + symbol
        return url.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
    }

    public static void removeAllWithStringPrefix(AbstractCache<String, ?> cache, String urlPrefix) {
        Set<String> keys = cache.keySet();

        for (String key : keys) {
            if (key.startsWith(urlPrefix)) {
                cache.remove(key);
            }
        }

        if (cache.isDiskCacheEnabled()) {
            removeExpiredCache(cache, urlPrefix);
        }
    }

    private static void removeExpiredCache(final AbstractCache<String, ?> cache,
            final String urlPrefix) {
        final File cacheDir = new File(cache.getDiskCacheDirectory());

        if (!cacheDir.exists()) {
            return;
        }

        File[] list = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return dir.equals(cacheDir)
                        && filename.startsWith(cache.getFileNameForKey(urlPrefix));
            }
        });

        if (list == null || list.length == 0) {
            return;
        }

        for (File file : list) {
            file.delete();
        }
    }

}
