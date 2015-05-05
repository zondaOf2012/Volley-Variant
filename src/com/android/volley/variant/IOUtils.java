package com.android.volley.variant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

public final class IOUtils {

  private IOUtils() {}

  /**
   * <p>
   * 获取对指定文件目录; <strong>当存在SdCard时,返回以SdCard为根目录; 反之,则返回以本应用程序的系统文件为根目录. </strong>
   * </p>
   * 
   * @param relativePath
   * @param context
   * 
   * @return
   */
  public static File getDirectory(String relativePath, Context context) {

    File readFile = null;

    StringBuffer sb = new StringBuffer();

    if (relativePath.startsWith(File.separator)) {

      sb.append(getRootEnableDir(context).toString()).append(relativePath);
    } else {

      sb.append(getRootEnableDir(context).toString()).append(File.separator).append(relativePath);
    }

    readFile = new File(sb.toString());

    return readFile;
  }

  /**
   * 获取一个可用根目录,SDcard优先
   * 
   * @param context
   * @return 可用根目录,若存在SDcard,则直接返回SDcard根目录;若不存在SDcard,则返回包根目录
   */
  public static File getRootEnableDir(Context context) {

    if (isExitStorageDevice()) {

      return Environment.getExternalStorageDirectory();
    } else {

      return context.getFilesDir();
    }
  }

  /**
   * 存在合法(支持读写)的存储设备
   * 
   * @return
   */
  public static boolean isExitStorageDevice() {

    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

      return true;
    } else {

      return false;
    }
  }

  public static Bundle mapToBundle(Map<String, String> map) {

    if(map == null){
      return null;
    }
    
    Bundle bundle = new Bundle();

    String key = null, value = null;

    for (Map.Entry<String, String> entry : map.entrySet()) {

      key = entry.getKey();

      value = entry.getValue();

      if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
        continue;
      }
      bundle.putString(key, value);
    }

    return bundle;
  }

  public static Map<String, String> bundleToMap(Bundle bundle) {
    
    if(bundle == null){
      return null;
    }

    Map<String, String> map = new HashMap<String, String>();

    String value = null;

    for (String key : bundle.keySet()) {

      value = bundle.getString(key);

      if (TextUtils.isEmpty(value)) {

        continue;
      }
      map.put(key, value);
    }
    return map;
  }
}
