package com.handwriting.component;

/**
 * Created by Ravi on 19/03/2016.
 */

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

/**
 * This class defines a singleton pattern for define RequestQueue
 */
public class RequestManager {

    private static RequestManager instance;
    private RequestQueue requestQueue;
    private static Context context;

    private RequestManager(Context context) {
        this.context = context;
        this.requestQueue = getRequestQueue();
    }

    public static synchronized RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestManager(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {

            // Instantiate the cache
            Cache cache = new DiskBasedCache(context.getCacheDir(), 5 * 1024 * 1024); // 1MB cap
            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            requestQueue = new RequestQueue(cache, network);
        }

        return requestQueue;
    }

    public <T> void addRequest(Request<T> request) {
        getRequestQueue().add(request);
    }

}
