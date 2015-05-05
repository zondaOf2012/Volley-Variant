package com.android.volley.variant;

import java.io.File;

import android.content.Context;

import com.android.volley.toolbox.DiskBasedCache;


public class VariantCacheBuilder{

  private final static String sRootDirName = "Zonda";

  private final static int DEFAULT_DISK_USAGE_BYTES = 10 * 1024 * 1024;

  private Context mAppContext;

  public VariantCacheBuilder(Context context) {

    mAppContext = context;
  }
  
  public DiskBasedCache buildCache(){
    
    File rootDirectory = IOUtils.getDirectory(sRootDirName, mAppContext);
    
    DiskBasedCache basedCache = new DiskBasedCache(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    
    return basedCache;
  }

}
