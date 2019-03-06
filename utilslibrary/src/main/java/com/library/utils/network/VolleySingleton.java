package com.library.utils.network;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Shripal17 on 09-07-2017.
 */

public class VolleySingleton {
  
  private static VolleySingleton mInstance = null;
  private RequestQueue mRequestQueue;
  
  private VolleySingleton(Context ctx) {
    mRequestQueue = Volley.newRequestQueue(ctx);
  }
  
  public static VolleySingleton getInstance(Context ctx) {
    if (mInstance == null) {
      mInstance = new VolleySingleton(ctx);
    }
    return mInstance;
  }
  
  public RequestQueue getRequestQueue() {
    return mRequestQueue;
  }
  
  public void cancelPendingRequests(Object tag) {
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(tag);
    }
  }
  
  public <T> void addToRequestQueue(Request<T> req, String tag) {
    getRequestQueue().getCache().clear();
    req.setTag(tag);
    getRequestQueue().add(req);
  }
  
  public <T> void addToRequestQueue(Request<T> req) {
    getRequestQueue().getCache().clear();
    getRequestQueue().add(req);
  }
}
