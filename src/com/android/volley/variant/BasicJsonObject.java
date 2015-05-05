package com.android.volley.variant;

import java.io.Serializable;
import java.lang.reflect.Type;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.zakergson.Gson;
import com.google.zakergson.GsonBuilder;
import com.google.zakergson.JsonSyntaxException;
import com.google.zakergson.annotations.SerializedName;

public abstract class BasicJsonObject implements Parcelable, Serializable {

  private static final long serialVersionUID = 397955714216778727L;

  /**
   * Json数据对应的接口版本号
   */
  @SerializedName("api_version")
  private String objectApiVersion;

  /**
   * Json数据的最后跟新时间
   */
  @SerializedName("last_time")
  private long objectLastTime;

  public BasicJsonObject() {

    objectApiVersion = "1.0";

    objectLastTime = System.currentTimeMillis();
  }

  protected BasicJsonObject(Parcel in) {

    objectApiVersion = in.readString();

    objectLastTime = in.readLong();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(objectApiVersion);
    dest.writeLong(objectLastTime);
  }

  public abstract Type getGsonType();

  /**
   * 自定义Gson构造方法
   * 
   * @return
   */
  protected Gson gsonBuilder() {

    Gson gson = new GsonBuilder().create();

    return gson;
  }

  public final long getObjectLastTime() {
    return objectLastTime;
  }

  public final void setObjectLastTime(long objectLastTime) {
    this.objectLastTime = objectLastTime;
  }

  /**
   * <p>
   * 将 apiObject转化为 jsongString
   * </p>
   * 
   * @param apiObject 此对象不可为null,否则转化失败
   * @return
   */
  public static <T extends BasicJsonObject> String toJson(T apiObject) {

    if (apiObject == null) {

      return null;
    }

    Gson gson = apiObject.gsonBuilder();

    return gson.toJson(apiObject, apiObject.getGsonType());
  }

  /**
   * <p>
   * 将 jsongString 转化为 apiObject
   * </p>
   * 
   * @param apiObject 此对象不可为null,否则转化失败
   * @param jsonString
   * @return
   * @see #toJson(BasicJsonObject)
   * @see #gsonBuilder()
   */
  public static <T extends BasicJsonObject> T convertFromJson(T apiObject, String jsonString) {

    T convertObj = null;

    if (apiObject != null) {

      try {
        Gson gson = apiObject.gsonBuilder();

        convertObj = gson.fromJson(jsonString, apiObject.getGsonType());
      } catch (JsonSyntaxException e) {
        e.printStackTrace();
      }
    }

    return convertObj == null ? apiObject : convertObj;
  }

}
