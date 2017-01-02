package com.scalpr.scalpr.Helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scalpr.scalpr.Objects.HttpResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cam on 9/19/2016.
 */
public class BingImageSearchHelper {

    Context c;

    public BingImageSearchHelper(Context _c){
        c = _c;
    }

    public String[] getImageThumbsFromJSON(String json){
        String[] imageURLs = new String[9];
        boolean noError = true;

        try{
            JSONObject jObject = new JSONObject(json);
            JSONArray jArray = jObject.getJSONArray("value");

            for(int i = 0; i < imageURLs.length; i++){
                imageURLs[i] = jArray.getJSONObject(i).getString("thumbnailUrl");
            }

        }catch (Exception ex){
            noError = false;
            Log.d("ERROR",ex.toString());
            Log.d("ERROR",json);
        }
        return imageURLs;
    }

    public void BingImageRequest(final HttpResponseListener responseListener, String query){
        responseListener.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.cognitive.microsoft.com")
                .appendPath("bing")
                .appendPath("v5.0")
                .appendPath("images")
                .appendPath("search")
                .appendQueryParameter("q", query)
                .appendQueryParameter("count", "10")
                .appendQueryParameter("offset", "0")
                .appendQueryParameter("mkt", "en-us")
                .appendQueryParameter("safeSearch", "Moderate");

        final String URL = builder.build().toString();

        StringRequest sr = new StringRequest(Request.Method.POST,URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseListener.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.requestEndedWithError(error);
                Log.d("ERROR",error.toString());
                Log.d("ERROR",URL);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Ocp-Apim-Subscription-Key", "eda851850263475eafeab76e243880ac");
                return params;
            }
        };
        queue.add(sr);
    }

}
