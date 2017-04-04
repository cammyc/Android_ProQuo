package com.scalpr.scalpr.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Created by Cam on 9/22/2016.
 */
public class MiscHelper {

    public static boolean showNotification = true;

    public static int getPostColor(Context c, int postType){
        return (postType == 2) ? ResourcesCompat.getColor(c.getResources(), R.color.requestTicket, null) :  ResourcesCompat.getColor(c.getResources(), R.color.sellTicket, null);
    }

    public static String formatDouble(double doub){
        String price = "";

        if(doub % 1 == 0){
            int val = (int) doub;
            price = val + "";
        }else{
            price = doub + "";
        }

        return price;
    }

    public String formatMarkerPrice(long price){
        return format(price) + "";
    }

    public static String formatDate(String dateString){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = "";

        try {
            date = format.parse(dateString);
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            formattedDate = df.format(date);
        }catch (Exception ex){
            formattedDate = dateString;
        }
        return formattedDate;
    }

    public static String formatDate(String dateString, String stringFormat){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = "";

        try {
            date = format.parse(dateString);
            SimpleDateFormat df = new SimpleDateFormat(stringFormat);
            formattedDate = df.format(date);
        }catch (Exception ex){
            formattedDate = dateString;
        }
        return formattedDate;
    }

    public static String formatDate(Date date, String stringFormat){
        String formattedDate = "";

        try {
            SimpleDateFormat df = new SimpleDateFormat(stringFormat);
            formattedDate = df.format(date);
        }catch (Exception ex){

        }
        return formattedDate;
    }

    public static Date parseUTCStringToDate(String dateString, String dateFormat){
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDateToEnglish(String dateString){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = "";

        try {
            date = format.parse(dateString);
            SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
            formattedDate = df.format(date);
        }catch (Exception ex){
            formattedDate = dateString;
        }
        return formattedDate;
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public void checkForUpdateRequest(final HttpResponseListener responseListener, Context c){
        responseListener.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/minimum_app_version_android.php", new Response.Listener<String>() {
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

}
