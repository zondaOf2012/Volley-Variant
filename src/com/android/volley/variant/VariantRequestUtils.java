package com.android.volley.variant;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;

public final class VariantRequestUtils {

  private final static String sPostBoundary = "et567z";

  private final static String sPostContentTypeName = "Multipart/form-data";

  private final static String sLineFeed = "\r\n";

  public final static String CLIENT_POST_REQUEST_PIC_PARAMS_KEY_PREFIX =
      "client_post_request_pic_params_key_prefix_";

  private VariantRequestUtils() {}

  public static final String getMultipartFormContentType() {

    StringBuffer buffer = new StringBuffer();

    buffer.append(sPostContentTypeName).append(";boundary=").append(sPostBoundary);

    return buffer.toString();
  }

  /**
   * 
   * @param params 参数键值对(必须确保此参数键值对均已Encode)
   * @return
   */
  public static final byte[] getMultipartFormByteDatas(Map<String, String> params)
      throws IOException {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    String key = null, value = null;

    String imageKeyName = null, imageValueName = null;

    byte[] imageContentBytes = null;

    for (Entry<String, String> entry : params.entrySet()) {

      key = entry.getKey();

      value = entry.getValue();

      // 如果KEY或VALUE为Empty，则不再处理该请求参数
      if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {

        continue;
      }

      // 如果KEY的前缀不是“图片参数请求KEY的前缀”，则按普通参数拼接
      // 否则按图片文件参数拼接
      if (!isFilePathKey(key)) {

        StringBuffer normalParamBuffer = new StringBuffer();

        normalParamBuffer.append("--").append(sPostBoundary).append(sLineFeed);

        normalParamBuffer.append("Content-Disposition: from-data; name=\"").append(key)
            .append("\"").append(sLineFeed).append(sLineFeed).append(value).append(sLineFeed);

        outputStream.write(normalParamBuffer.toString().getBytes());
      } else {

        StringBuffer fileParamBuffer = new StringBuffer();

        fileParamBuffer.append("--").append(sPostBoundary).append(sLineFeed);

        imageKeyName = parseImageJpgKeyName(key);

        imageValueName = parseImageJpgValueName(value);

        if (TextUtils.isEmpty(imageKeyName) || TextUtils.isEmpty(imageValueName)) {

          throw new IOException("request's image key or value is error !");
        }

        imageContentBytes = getImageJpgFileByteDatas(value);

        fileParamBuffer.append("Content-Disposition: from-data; name=\"").append(imageKeyName)
            .append("\";filename=\"").append(imageValueName).append("\"\r\n")
            .append("Content-Type: image/jpg\r\n\r\n");

        outputStream.write(fileParamBuffer.toString().getBytes());

        outputStream.write(imageContentBytes);

        outputStream.write(sLineFeed.getBytes());
      }// end of if-else
    }// end of for

    StringBuffer endBuffer = new StringBuffer();

    endBuffer.append("--").append(sPostBoundary).append("--").append(sLineFeed);

    outputStream.write(endBuffer.toString().getBytes());

    return outputStream.toByteArray();
  }

  public static final String buildImageJpgKeyName(String paramKeyName) {

    StringBuffer buffer = new StringBuffer(CLIENT_POST_REQUEST_PIC_PARAMS_KEY_PREFIX);

    buffer.append(paramKeyName);

    return buffer.toString();
  }

  private static final String parseImageJpgKeyName(String imageJpgKeyName) {

    if (TextUtils.isEmpty(imageJpgKeyName)) {

      return imageJpgKeyName;
    } else {

      return imageJpgKeyName.substring(CLIENT_POST_REQUEST_PIC_PARAMS_KEY_PREFIX.length());
    }
  }

  static final boolean isFilePathKey(String key) {

    return key.startsWith(CLIENT_POST_REQUEST_PIC_PARAMS_KEY_PREFIX);
  }

  private static final String parseImageJpgValueName(String paramValueName) {

    if (TextUtils.isEmpty(paramValueName)) {

      return "temp.jpg";
    }

    return MD5Utils.encodeBy16BitMD5(paramValueName) + ".jpg";
  }

  private static final byte[] getImageJpgFileByteDatas(String fileName) throws IOException {

    File imageFile = new File(fileName);

    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(imageFile));

    int len = inputStream.available();

    byte[] bytes = new byte[len];

    int readLen = inputStream.read(bytes);

    if (len != readLen) {

      bytes = null;

      inputStream.close();

      throw new IOException("read imge file is error");
    }

    inputStream.close();

    return bytes;
  }
}
