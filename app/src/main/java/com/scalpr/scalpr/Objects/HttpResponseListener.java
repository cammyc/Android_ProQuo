package com.scalpr.scalpr.Objects;

import com.android.volley.VolleyError;

/**
 * Created by Cam on 9/18/2016.
 */
public interface HttpResponseListener {
    public void requestStarted();
    public void requestCompleted(String response);
    public void requestEndedWithError(VolleyError error);
}
