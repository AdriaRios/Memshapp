package org.adriarios.memshapp.adapter;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Adrian on 23/03/2015.
 */
public class ImagesData
{
    private static ImagesData instance;

    public static String customVar="Hello";
    public LruCache<String, Bitmap> mMemoryCache;

    public static void initInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new ImagesData();
        }
    }

    public static ImagesData getInstance()
    {
        // Return the instance
        if (instance == null)
        {
            // Create the instance
            instance = new ImagesData();

            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;

            instance.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        return instance;
    }

    private ImagesData()
    {
        // Constructor hidden because this is a singleton
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

}
