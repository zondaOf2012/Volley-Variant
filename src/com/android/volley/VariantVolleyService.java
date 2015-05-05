package com.android.volley;

import java.util.Map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.android.volley.Request.Method;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.variant.BasicJsonResult;
import com.android.volley.variant.IOUtils;
import com.android.volley.variant.VariantHttpStack;
import com.android.volley.variant.VariantRequest;

public abstract class VariantVolleyService<T extends BasicJsonResult> extends IntentService {

  private final static String sName = "com.android.volley.VariantVolleyService";
  
  private final static int sDefaultMethod = Method.POST;

  protected final static String ARG_BROADCAST_ACTION_KEY = "arg_broadcast_action_key";

  protected final static String ARG_REQUEST_METHOD_KEY = "arg_request_method_key";

  protected final static String ARG_REQUEST_URL_KEY = "arg_request_url_key";

  protected final static String ARG_REQUEST_PARAMS_KEY = "arg_request_params_key";

  public VariantVolleyService() {

    super(sName);
  }

  @Override
  protected final void onHandleIntent(Intent intent) {

    if (intent == null) {
      return;
    }

    String originUrl = intent.getStringExtra(ARG_REQUEST_URL_KEY);

    if (TextUtils.isEmpty(originUrl)) {

      return;
    }

    int requestMethod = intent.getIntExtra(ARG_REQUEST_METHOD_KEY, sDefaultMethod);

    Bundle requestParamsBundle = intent.getBundleExtra(ARG_REQUEST_PARAMS_KEY);

    Map<String, String> params = IOUtils.bundleToMap(requestParamsBundle);

    Request<T> variantRequest = getVariantRequest(requestMethod, originUrl, params);

    variantRequest.setShouldCache(false);

    Bundle result = null;

    HttpStack httpStack = new VariantHttpStack();

    Network network = new BasicNetwork(httpStack);

    long startTimeMs = SystemClock.elapsedRealtime();

    try {
      NetworkResponse networkResponse = network.performRequest(variantRequest);

      Response<T> response = variantRequest.parseNetworkResponse(networkResponse);

      result = parseResponse(response);
    } catch (VolleyError volleyError) {

      volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
      volleyError = variantRequest.parseNetworkError(volleyError);
      result = parseError(volleyError);
    } catch (Exception e) {
      VolleyLog.e(e, "Unhandled exception %s", e.toString());
      VolleyError volleyError = new VolleyError(e);
      volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
      result = parseError(volleyError);
    }

    String action = intent.getStringExtra(ARG_BROADCAST_ACTION_KEY);

    if (!TextUtils.isEmpty(action)) {

      deliverResult(result, action);
    }
  }

  abstract protected VariantRequest<T> getVariantRequest(int method, String url, Map<String, String> params);

  abstract protected Bundle parseResponse(Response<T> response);

  abstract protected Bundle parseError(VolleyError error);

  protected void deliverResult(Bundle bundle, String action) {

    Intent deliverIntent = new Intent(action);

    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(deliverIntent);
  }

  public final static class Builder {

    private Map<String, String> params;

    private String url;

    private int method = sDefaultMethod;

    private String resultAction;

    public Builder() {

    }

    public Intent build(Context packageContext, Class<?> className) {

      Intent intent = new Intent(packageContext, className);

      intent.putExtra(ARG_REQUEST_URL_KEY, url);

      intent.putExtra(ARG_REQUEST_METHOD_KEY, method);

      intent.putExtra(ARG_BROADCAST_ACTION_KEY, resultAction);

      intent.putExtra(ARG_REQUEST_PARAMS_KEY, IOUtils.mapToBundle(params));

      return intent;
    }

    public final Builder setParams(Map<String, String> params) {

      this.params = params;

      return this;
    }

    public final Builder setUrl(String url) {

      this.url = url;

      return this;
    }

    public final Builder setMethod(int method) {

      this.method = method;

      return this;
    }

    public final Builder setResultAction(String resultAction) {

      this.resultAction = resultAction;

      return this;
    }
  }
}
