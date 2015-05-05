package com.android.volley.variant;

import com.android.volley.VolleyError;


public interface OnVariantResponseListener<T> {

  public void onResponse(boolean intermediate, T response);

  public void onErrorResponse(boolean intermediate, VolleyError error);
}
