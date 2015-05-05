package com.android.volley.variant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.zakergson.JsonSyntaxException;

public abstract class VariantRequest<T extends BasicJsonResult> extends Request<T> {

  /** Default charset for JSON request. */
  protected static final String PROTOCOL_CHARSET = "utf-8";

  private static final long sDefaultCacheMaxAge = 10 * 60 * 1000L;// 十分钟

  private static final long sDefaultCacheTtl = 30 * 24 * 60 * 60 * 1000L;// 30天

  private OnVariantResponseListener<T> mResponseListener;

  private final Map<String, String> mRequestParams;

  private long mCacheMaxAge = sDefaultCacheMaxAge;

  public VariantRequest(int method, String url) {

    super(method, url, null);

    mRequestParams = new HashMap<String, String>();
    
    setRetryPolicy(new VariantRetryPolicy());
  }

  public abstract T getJsonResultEntity();

  @Override
  public Response<T> parseNetworkResponse(NetworkResponse response) {

    try {

      VolleyLog.v("ZBasedRequest parseNetworkResponse response headers: %s",
          response.headers.toString());

      String charset = HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET);

      String responseStr = new String(response.data, charset);

      Cache.Entry cacheEntry = parseCache(response);

      T result = BasicJsonResult.convertFromJson(getJsonResultEntity(), responseStr);

      Response<T> serverResponse = Response.success(result, cacheEntry);

      return serverResponse;

    } catch (JsonSyntaxException e) {

      return Response.error(new ParseError(e));
    } catch (UnsupportedEncodingException e) {

      return Response.error(new ParseError(e));
    } catch (JSONException e) {

      return Response.error(new ParseError(e));
    }
  }

  protected final Entry parseCache(NetworkResponse response) {

    long now = System.currentTimeMillis();

    Cache.Entry entry = new Cache.Entry();

    entry.ttl = now + sDefaultCacheTtl;

    entry.softTtl = now + mCacheMaxAge;

    VolleyLog.v("ZBasedRequest parseCache ttl: %s - currentTimeMillis: %s - softTtl: %s",
        entry.ttl, System.currentTimeMillis(), entry.softTtl);

    Map<String, String> headers = response.headers;

    long serverDate = 0;

    long lastModified = 0;

    String headerValue = headers.get("Date");

    if (headerValue != null) {
      serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
    }

    headerValue = headers.get("Last-Modified");

    if (headerValue != null) {

      lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
    }

    entry.data = response.data;
    entry.etag = headers.get("ETag");
    entry.serverDate = serverDate;
    entry.lastModified = lastModified;
    entry.responseHeaders = headers;

    return entry;
  }

  @Override
  protected void deliverResponse(T response) {

  }

  @Override
  public void deliverResponse(Response<T> response) {

    if (mResponseListener != null) {

      if (response.isSuccess()) {

        mResponseListener.onResponse(response.intermediate, response.result);
      } else {

        mResponseListener.onErrorResponse(response.intermediate, response.error);
      }
    }
  }

  @Override
  public final String getCacheKey() {

    return super.getUrl();
  }

  @Override
  public final String getUrl() {

    String url = super.getUrl();

    if (isSameForPostMethod()) {
      // do nothing
    } else {

      url = appendParams(url);
    }

    return url;
  }

  private String appendParams(String originUrl) {

    final StringBuffer accessedUrlBuffer = new StringBuffer();

    final StringBuffer originUrlBuffer = new StringBuffer(originUrl);

    // 确保URL地址的最后一位不带“/”
    if (originUrlBuffer.toString().endsWith("/")) {

      originUrlBuffer.deleteCharAt(originUrlBuffer.length() - 1);
    }

    int index = originUrlBuffer.indexOf("?");

    // 得到一个不带参数的URL
    String cleanUrl = null;

    if (index != -1) {

      cleanUrl = originUrlBuffer.substring(0, index).trim();
    } else {

      cleanUrl = originUrlBuffer.toString().trim();
    }

    accessedUrlBuffer.append(cleanUrl);

    final Map<String, String> allParams = new HashMap<String, String>();

    allParams.putAll(mRequestParams);

    if (index != -1) {

      cutFieldToParamMap(index, originUrlBuffer, allParams);
    }

    // 根据full_args过滤参数
    // paramMap = WebRequestParamsUtils.filterBaseParamsByFullArgs(allParams);

    // 排序
    List<Map.Entry<String, String>> orderedParamList = sortParams(allParams);

    if (orderedParamList != null && !orderedParamList.isEmpty()) {

      accessedUrlBuffer.append("?");

      String key = null, value = null;

      for (Map.Entry<String, String> paramEntry : orderedParamList) {

        value = paramEntry.getValue();

        key = paramEntry.getKey();

        if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(key)) {

          accessedUrlBuffer.append(key);
          accessedUrlBuffer.append("=");
          accessedUrlBuffer.append(value);
          accessedUrlBuffer.append("&");
        }
      }
    }

    // 得到一个拼装好所有参数的URL地址
    accessedUrlBuffer.deleteCharAt(accessedUrlBuffer.length() - 1);

    final String accessedUrl = accessedUrlBuffer.toString();

    VolleyLog.v("ZBasedRequest appendParams accessedUrl: %s", accessedUrl);

    return accessedUrl;
  }

  /**
   * 将StringBuffer中的参数以键值对的形式存入storedMap中
   * 
   * @param index
   * @param sb
   */
  private void cutFieldToParamMap(int index, StringBuffer sb, Map<String, String> storedMap) {

    String temp = sb.toString().substring(index + 1);

    while (true) {

      int last_key = temp.indexOf('=');
      int last_value = temp.indexOf('&');
      String key = "";
      String value = "";

      // 如果截到最后一个,退出
      if (last_value == -1 || temp.trim().length() <= 1) {

        key = temp.substring(0, last_key);
        value = temp.substring(last_key + 1);
        // 对于从URL尾部截取的key和value是不用再Encode，且是优先的
        addRequestParam(storedMap, key, value, false, true);
        break;
      } else {

        key = temp.substring(0, last_key);
        value = temp.substring(last_key + 1, last_value);
        // 对于从URL尾部截取的key和value是不用再Encode，且是优先的
        addRequestParam(storedMap, key, value, false, true);
      }

      temp = temp.substring(last_value + 1);
    }// end of while
    VolleyLog.v("ZBasedRequest cutFieldToParamMap : %s", storedMap.toString());
  }


  private boolean isSameForPostMethod() {

    int method = getMethod();

    switch (method) {
      case Method.POST:
      case Method.PUT:
      case Method.PATCH:
        return true;
      default:
        return false;
    }
  }

  private List<Map.Entry<String, String>> sortParams(Map<String, String> params) {
    if (params == null || params.size() == 0) {
      return null;
    }
    List<Map.Entry<String, String>> paramsList =
        new ArrayList<Map.Entry<String, String>>(params.entrySet());

    Collections.sort(paramsList, new Comparator<Map.Entry<String, String>>() {

      @Override
      public int compare(Map.Entry<String, String> object1, Map.Entry<String, String> object2) {
        return object1.getKey().compareTo(object2.getKey());
      }
    });
    return paramsList;
  }

  @Override
  public String getBodyContentType() {

    return VariantRequestUtils.getMultipartFormContentType();
  }

  @Override
  public byte[] getBody() throws AuthFailureError {

    byte[] contentByteDatas = null;

    try {
      contentByteDatas = VariantRequestUtils.getMultipartFormByteDatas(mRequestParams);
    } catch (IOException e) {

      VolleyLog.d("ZBasedRequest getBody is cause exception: %s", e.getMessage());
    }

    return contentByteDatas;
  }

  /**
   * @param nowParamMap
   * @param paramKey 请求的key值
   * @param paramValue 请求的value值
   * @param shouldEncode 对应的key和value是否需要encode，为true则encode，反之不再encode
   * @param isPrioritized 是否因为优先的key和value，为true则当{@link #mRequestParams}
   *        中即便已存在改key值，那么此key值也将覆盖之前的值；反之，不再添加直接返回
   */
  private final void addRequestParam(Map<String, String> nowParamMap, String paramKey,
      String paramValue, boolean shouldEncode, boolean isPrioritized) {

    if (nowParamMap == null || TextUtils.isEmpty(paramKey) || TextUtils.isEmpty(paramValue)) {
      return;
    }

    String key = null, value = null;

    if (shouldEncode) {

      try {

        key = URLEncoder.encode(paramKey, getParamsEncoding());

        value = URLEncoder.encode(paramValue, getParamsEncoding());

      } catch (UnsupportedEncodingException e) {

        VolleyLog.d("%s or %s is UnsupportedEncoding: %s", paramKey, paramValue,
            getParamsEncoding());
        return;
      }// end of try-catch
    } else {

      key = paramKey;

      value = paramValue;
    }

    // 若改key值已存在，且当前不key值不是优先的则不再添加
    if (!isPrioritized && nowParamMap.containsKey(key)) {
      return;
    }

    nowParamMap.put(key, value);
  }

  /**
   * 添加请求参数，<strong>注意：此方法的两个参数是不允许自行Encode，而是统一交个它来执行Encode操作</strong>
   * 
   * @param paramKey 请求的key值
   * @param paramValue 请求的value值
   * @see #addRequestParams(Map)
   */
  public final void addRequestParam(String paramKey, String paramValue) {

    if (VariantRequestUtils.isFilePathKey(paramKey)) {

      // 对于文件路径类型的参数是不用Encode的,且是可被覆盖的(即是优先的)
      addRequestParam(mRequestParams, paramKey, paramValue, false, true);
    } else {

      // 对于客户端自己添加的参数需要统一Encode，但不是优先的
      addRequestParam(mRequestParams, paramKey, paramValue, true, false);
    }
  }

  /**
   * 添加请求参数
   * 
   * @param params
   * @see #addRequestParam(String, String)
   */
  public final void addRequestParams(Map<String, String> params) {

    if (params == null) {
      return;
    }

    for (Map.Entry<String, String> entry : params.entrySet()) {

      addRequestParam(entry.getKey(), entry.getValue());
    }
  }

  /**
   * 设定请求的本地缓存时间(单位:毫秒)
   * 
   * @param cacheMaxAge
   */
  public void setCacheMaxAge(long cacheMaxAge) {
    this.mCacheMaxAge = cacheMaxAge;
  }

  public final void setResponseListener(OnVariantResponseListener<T> responseListener) {
    this.mResponseListener = responseListener;
  }

}
