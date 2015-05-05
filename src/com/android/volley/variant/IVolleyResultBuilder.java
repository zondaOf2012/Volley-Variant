package com.android.volley.variant;

import android.os.Bundle;

public interface IVolleyResultBuilder {

  public void parse(Bundle bundle);

  public Bundle build();
}
