package com.android.volley.variant;

import java.lang.reflect.Type;

import android.os.Parcel;

import com.google.zakergson.annotations.SerializedName;
import com.google.zakergson.reflect.TypeToken;

public class BlockInfoModel extends BasicJsonObject {

  private static final long serialVersionUID = 2441722386929809067L;

  @SerializedName("block_title")
  private String blockTitle;

  private String skey;

  public BlockInfoModel() {}

  public BlockInfoModel(Parcel in) {

    super(in);
    blockTitle = in.readString();
    skey = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

    super.writeToParcel(dest, flags);
    dest.writeString(blockTitle);
    dest.writeString(skey);
  }

  @Override
  public Type getGsonType() {

    return new TypeToken<BlockInfoModel>() {}.getType();
  }

  public final String getBlockTitle() {
    return blockTitle;
  }

  public final void setBlockTitle(String blockTitle) {
    this.blockTitle = blockTitle;
  }

  public final String getSkey() {
    return skey;
  }

  public final void setSkey(String skey) {
    this.skey = skey;
  }

}
