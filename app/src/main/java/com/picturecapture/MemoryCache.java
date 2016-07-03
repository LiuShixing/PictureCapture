package com.picturecapture;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by 明白 on 2016/5/11.
 */
public class MemoryCache {
    private LruCache mCache;
    public MemoryCache()
    {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 5;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mCache.put(key, bitmap);
        }
    }
    public Bitmap getBitmapFromMemoryCache(String key) {
        return (Bitmap) mCache.get(key);
    }
}
