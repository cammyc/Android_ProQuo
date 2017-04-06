package com.scalpr.scalpr.Helpers;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.Filters;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Attr;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cam on 9/19/2016.
 */
public class AttractionHelper {
    Context c;
    int TIMEOUT = 10000;
    RequestQueue queue;
    
    public AttractionHelper(Context _c){
        c = _c;
        queue = Volley.newRequestQueue(c);
    }

    public void PostAttractionRequest(final HttpResponseListener attractionPostResponse, final long userID, final String venueName, final String attractionName, final String ticketPrice, final String numberOfTickets, final String description, final String date, final String imageURL, final int postType, final double lat, final double lon){
        attractionPostResponse.requestStarted();
        
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/post_attraction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
               attractionPostResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                attractionPostResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("creatorID", userID + "");
                params.put("venueName",venueName);
                params.put("attractionName",attractionName);
                params.put("ticketPrice", ticketPrice);
                params.put("numberOfTickets",numberOfTickets);
                params.put("description",description);
                params.put("date",date);
                params.put("imageURL",imageURL);
                params.put("lat",String.valueOf(lat));
                params.put("lon",String.valueOf(lon));
                params.put("postType", postType + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };
        queue.add(sr);
    }

    public String attractionToJsonShort(Attraction a){
        JSONObject obj = new JSONObject();

        try {
            obj.put("attractionID", a.getID());
            obj.put("creatorID", a.getCreatorID());
            obj.put("venueName", a.getVenueName());
            obj.put("attractionName", a.getName());
            obj.put("ticketPrice", a.getTicketPrice());
            obj.put("numTickets", a.getNumTickets());
            obj.put("description", a.getDescription());
            obj.put("imageURL",a.getImageURL());
            obj.put("date", a.getDate());
            obj.put("postType", a.getPostType());
        }catch (Exception ex){

        }

        return obj.toString();
    }

    public void UpdateAttractionLocationRequest(final HttpResponseListener attractionPostResponse, final long userID, final long attractionID, final double lat, final double lon){
        attractionPostResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/update_attraction_location.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                attractionPostResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                attractionPostResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("creatorID", userID + "");
                params.put("attractionID", attractionID + "");
                params.put("lat",String.valueOf(lat));
                params.put("lon",String.valueOf(lon));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };
        queue.add(sr);
    }

    public void UpdateAttractionDetailsRequest(final HttpResponseListener attractionUpdateResponse, final long userID, final long attractionID, final String venueName, final String attractionName, final String ticketPrice, final String numberOfTickets, final String description, final String date, final String imageURL, final int postType){
        attractionUpdateResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/update_attraction_details.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                attractionUpdateResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                attractionUpdateResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("creatorID", userID + "");
                params.put("attractionID", attractionID + "");
                params.put("venueName",venueName);
                params.put("attractionName",attractionName);
                params.put("ticketPrice", ticketPrice);
                params.put("numberOfTickets",numberOfTickets);
                params.put("description",description);
                params.put("date",date);
                params.put("imageURL",imageURL);
                params.put("postType", postType + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };
        queue.add(sr);
    }

    public ArrayList<Attraction> getAttractions(String json){
        ArrayList<Attraction> attractions = new ArrayList<Attraction>();

        boolean noError = true;

        try{
            JSONArray jArray = new JSONArray(json);

            for(int i = 0; i < jArray.length(); i++){
                Attraction a = new Attraction();
                JSONObject jsonAttraction = jArray.getJSONObject(i);

                a.setID(jsonAttraction.getLong("attractionID"));
                a.setCreatorID(jsonAttraction.getLong("creatorID"));
                a.setVenueName(jsonAttraction.getString("venueName"));
                a.setName(jsonAttraction.getString("name"));
                a.setTicketPrice(jsonAttraction.getDouble("ticketPrice"));
                a.setNumTickets(jsonAttraction.getInt("numTickets"));
                a.setDescription(jsonAttraction.getString("description"));
                a.setDate(jsonAttraction.getString("date"));
                a.setImageURL(jsonAttraction.getString("imageURL"));
                a.setLat(jsonAttraction.getDouble("lat"));
                a.setLon(jsonAttraction.getDouble("lon"));
                a.setTimeStamp(jsonAttraction.getString("timeStamp"));
                a.setPostType(jsonAttraction.getInt("postType"));

                attractions.add(a);
            }

        }catch (Exception ex){
            noError = false;
            Log.d("ATTRACTION_JSON_ERROR",json);
        }

        return  attractions;
    }

    public void DeleteAttractionRequest(final HttpResponseListener responseListener, final long userID, final long attractionID){
        responseListener.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/delete_attraction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseListener.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("creatorID", userID + "");
                params.put("attractionID", attractionID + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };
        queue.add(sr);
    }

    public void getNewAttractionsRequest(final HttpResponseListener getUpdatedAttractionResponse, final Filters filter, final double latBoundLeft, final double latBoundRight, final double lonBoundLeft, final double lonBoundRight, final String searchViewQuery, final String IDs){
        getUpdatedAttractionResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_new_attractions.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getUpdatedAttractionResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getUpdatedAttractionResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("oldIDs", IDs);
                params.put("latBoundLeft",String.valueOf(latBoundLeft));
                params.put("latBoundRight",String.valueOf(latBoundRight));

                params.put("lonBoundLeft",String.valueOf(lonBoundLeft));
                params.put("lonBoundRight",String.valueOf(lonBoundRight));

                params.put("searchViewQuery",searchViewQuery);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(c.getTime());

                params.put("currentDate",formattedDate);

                Map<String, String> filters = new HashMap<String, String>();
                filters.put("startDate", filter.getStartDate());
                filters.put("endDate", filter.getEndDate());
                filters.put("requestedTickets", String.valueOf(filter.isShowRequested()));
                filters.put("sellingTickets", String.valueOf(filter.isShowSelling()));
                filters.put("minPrice", filter.getMinPrice()+"");
                filters.put("maxPrice", filter.getMaxPrice()+"");
                filters.put("numTickets", filter.getNumTickets()+"");

               JSONObject jsonFilters = new JSONObject(filters);

                params.put("jsonFilters", jsonFilters.toString());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(sr);
    }

    public void getInitialAttractionsRequest(final HttpResponseListener getAttractionResponse, final Filters filter, final double latBoundLeft, final double latBoundRight, final double lonBoundLeft, final double lonBoundRight){
        getAttractionResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_attractions.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getAttractionResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getAttractionResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("latBoundLeft",String.valueOf(latBoundLeft));
                params.put("latBoundRight",String.valueOf(latBoundRight));

                params.put("lonBoundLeft",String.valueOf(lonBoundLeft));
                params.put("lonBoundRight",String.valueOf(lonBoundRight));

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(c.getTime());

                params.put("currentDate",formattedDate);

                Map<String, String> filters = new HashMap<String, String>();
                filters.put("startDate", filter.getStartDate());
                filters.put("endDate", filter.getEndDate());
                filters.put("requestedTickets", String.valueOf(filter.isShowRequested()));
                filters.put("sellingTickets", String.valueOf(filter.isShowSelling()));
                filters.put("minPrice", filter.getMinPrice()+"");
                filters.put("maxPrice", filter.getMaxPrice()+"");
                filters.put("numTickets", filter.getNumTickets()+"");

                JSONObject jsonFilters = new JSONObject(filters);

                params.put("jsonFilters", jsonFilters.toString());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };

        sr.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(sr);
    }

    public void getUsersAttractionsRequest(final HttpResponseListener getUserAttractionsResponse, final long userID){
        getUserAttractionsResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_user_attractions.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getUserAttractionsResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getUserAttractionsResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("userID", userID + "");

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(c.getTime());

                params.put("currentDate",formattedDate);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.getAccessToken(c));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }
}
