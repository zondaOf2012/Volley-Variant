package com.android.volley.variant;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Build;

import com.android.volley.toolbox.HurlStack;

public class VariantHttpStack extends HurlStack{

  public VariantHttpStack() {
    
    super();
  }

  @Override
  protected HttpURLConnection createConnection(URL url) throws IOException {

    HttpURLConnection urlConnection = super.createConnection(url);
    
    urlConnection.setRequestProperty("User-Agent", RequestPropertyUtils.generateDefineUserAgent());
    
    return urlConnection;
  }
}
