package com.android.volley.variant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.VariantResponseDelivery;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;

public final class VolleyVariant {

  private static final int sThreadPoolSize = 3;

  public final static RequestQueue newRequestQueue(Context context) {

    VolleyLog.DEBUG = true;

    DiskBasedCache diskBasedCache = new VariantCacheBuilder(context).buildCache();

    HttpStack httpStack = new VariantHttpStack();

    Network network = new BasicNetwork(httpStack);

    Handler deliveryHandler = new Handler(Looper.getMainLooper());

    VariantResponseDelivery responseDelivery = new VariantResponseDelivery(deliveryHandler);

    RequestQueue requestQueue =
        new RequestQueue(diskBasedCache, network, sThreadPoolSize, responseDelivery);

    requestQueue.start();

    return requestQueue;
  }
  
}
