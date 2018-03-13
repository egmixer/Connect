package com.example.connectapp.utils;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;


public class PicassoCache {
    /**
     * Static Picasso Instance
     */
    private static Picasso picassoInstance = null;

    /**
     * PicassoCache Constructor
     */
    private PicassoCache(Context context) {
        Downloader downloader = new OkHttp3Downloader(context, Integer.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(downloader);

        picassoInstance = builder.build();
    }

    /**
     * Get Singleton Picasso Instance
     *
     * @return Picasso instance
     */
    public synchronized static Picasso get(Context context) {
        if (picassoInstance == null) {
            new PicassoCache(context);
            return picassoInstance;
        }

        return picassoInstance;
    }

}
