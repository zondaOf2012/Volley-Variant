package com.android.volley.variant;

import android.os.Build;

public final class RequestPropertyUtils {

  private RequestPropertyUtils() {}

  public static String generateDefineUserAgent() {

    StringBuffer sb = new StringBuffer();

    sb.append("Mozilla/5.0 (Linux; U; Android ")
        .append((Build.VERSION.RELEASE == null ? "4.0.4" : Build.VERSION.RELEASE))
        .append("; zh-cn;")
        .append((Build.MODEL == null ? "GT-N7000" : Build.MODEL))
        .append(
            " Build/JOP40D; CyanogenMod-10.1) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");

    return sb.toString();
  }
}
