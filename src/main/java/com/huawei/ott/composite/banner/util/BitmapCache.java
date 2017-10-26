package com.huawei.ott.composite.banner.util;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

/**
 * BitmapCache for adsBanner
 */

public class BitmapCache
{
    private LruCache<String, Bitmap> gradientBitmapMap;

    private LruCache<String, Bitmap> blurBitmapMap;

    private static BitmapCache instance = new BitmapCache();

    public static BitmapCache getInstance()
    {
        return instance;
    }

    private BitmapCache()
    {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        gradientBitmapMap = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap value)
            {
                return value.getByteCount();
            }

        };
        blurBitmapMap = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap value)
            {
                return value.getByteCount();
            }

        };
    }

    public void saveGradientBitmap(Bitmap bitmap, String ImageUri)
    {
        gradientBitmapMap.put(ImageUri, bitmap);
    }

    public void saveBlurBitmap(Bitmap bitmap, String ImageUri)
    {
        blurBitmapMap.put(ImageUri, bitmap);
    }

    /**
     * get the CacheGradientBitmap by imageUri
     *
     * @param imageUri the picture uri
     * @return the CacheGradientBitmap
     */
    public Bitmap getCacheGradientBitmap(String imageUri)
    {
        if (gradientBitmapMap != null && !TextUtils.isEmpty(imageUri))
        {
            return gradientBitmapMap.get(imageUri);
        }
        return null;
    }

    public Bitmap getCacheBlurBitmap(String imageUri)
    {
        if (blurBitmapMap != null && !TextUtils.isEmpty(imageUri))
        {
            return blurBitmapMap.get(imageUri);
        }
        return null;
    }

}

