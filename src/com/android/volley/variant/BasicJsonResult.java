package com.android.volley.variant;

import java.lang.reflect.Type;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.text.TextUtils;

import com.google.zakergson.Gson;
import com.google.zakergson.JsonSyntaxException;
import com.google.zakergson.annotations.SerializedName;
import com.google.zakergson.reflect.TypeToken;

public class BasicJsonResult extends BasicJsonObject {

  private static final long serialVersionUID = -6286688496861117346L;

  private final static transient String CLIENT_DEFAULT_STATE = "client_default_state";

  @SerializedName("stat")
  private String state;

  private String msg;

  public BasicJsonResult() {

    state = CLIENT_DEFAULT_STATE;
  }

  public BasicJsonResult(Parcel in) {
    super(in);
  }

  @Override
  public Type getGsonType() {
    return new TypeToken<BasicJsonResult>() {}.getType();
  }

  public static boolean isNormal(BasicJsonResult result) {

    return result != null && "1".equals(result.state);
  }

  /**
   * 
   * @param proResult 此参数不可为空，否则无法转换
   * @param wsResult
   * @return
   */
  public static <T extends BasicJsonResult> T convertFromJson(T proResult, String jsonString)
      throws JsonSyntaxException, JSONException {

    T convertResult = null;

    if (proResult != null && !TextUtils.isEmpty(jsonString)) {

      JSONObject jsonObject = new JSONObject(jsonString);

      proResult.setState(jsonObject.optString("stat", "server's json don't contain stat key"));

      proResult.setMsg(jsonObject.optString("msg", "server's json don't contain msg key"));

      if (isNormal(proResult)) {

        Gson gson = proResult.gsonBuilder();

        String dataJsonStr = jsonObject.optString("data");

        if (!TextUtils.isEmpty(dataJsonStr)) {

          convertResult = gson.fromJson(dataJsonStr, proResult.getGsonType());

          convertResult.setState(proResult.getState());

          convertResult.setMsg(proResult.getMsg());
        }
      }
    }
    return convertResult == null ? proResult : convertResult;
  }

  public final String getState() {
    return state;
  }

  public final String getMsg() {
    return msg;
  }

  public final void setState(String state) {
    this.state = state;
  }

  public final void setMsg(String msg) {
    this.msg = msg;
  }
}
