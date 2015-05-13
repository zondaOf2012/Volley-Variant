package com.android.volley.variant;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.json.JSONException;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.Cache.Entry;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.zakergson.JsonSyntaxException;

public abstract class VarianJsonRequest<T extends BasicJsonResult> extends VariantRequest<T> {
  
  private long mCacheMaxAge = sDefaultCacheMaxAge;
  
  private static final long sDefaultCacheMaxAge = 10 * 60 * 1000L;// 十分钟

  private static final long sDefaultCacheTtl = 30 * 24 * 60 * 60 * 1000L;// 30天

  public VarianJsonRequest(int method, String url) {
    super(method, url);
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

  /**
   * 设定请求的本地缓存时间(单位:毫秒)
   * 
   * @param cacheMaxAge
   */
  public void setCacheMaxAge(long cacheMaxAge) {
    this.mCacheMaxAge = cacheMaxAge;
  }
}
