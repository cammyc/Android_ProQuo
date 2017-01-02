package com.scalpr.scalpr.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.scalpr.scalpr.BackgroundService.ConversationUpdateService;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.User;
import com.scalpr.scalpr.R;

import org.json.JSONObject;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.glide.transformations.internal.Utils;

/**
 * Created by Cam on 9/18/2016.
 */
public class UserHelper {
    Context c;

    public UserHelper(Context _c){
        c = _c;
    }

    public void confirmLoginAndSaveUser(String json){
        boolean noError = true;
        long userID = 0;
        String firstName = "";
        String lastName = "";
        String email = "";
        String phone = "";
        String password = "";
        String displayPicURL = "";

        try{
            JSONObject jObject = new JSONObject(json);
            userID = jObject.getLong("userID");
            firstName = jObject.getString("firstName");
            lastName = jObject.getString("lastName");
            email = jObject.getString("email");
            phone = jObject.getString("phoneNumber");
            password = jObject.getString("password");
            displayPicURL = jObject.getString("displayPicURL");

        }catch (Exception ex){
            noError = false;
            Log.d("ERROR",ex.toString());
            Log.d("ERROR",json);
        }

        if(noError){
            SharedPreferences.Editor sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
            sharedPref.putLong("userID", userID);
            sharedPref.putString("firstName",firstName);
            sharedPref.putString("lastName",lastName);
            sharedPref.putString("email",email);
            sharedPref.putString("phoneNumber",phone);
            sharedPref.putString("password",password);
            sharedPref.putString("displayPicURL",displayPicURL);
            sharedPref.commit();
        }
    }

    public User getUserDetailsFromJson(String json){
        User u = new User();
        try{
            JSONObject jObject = new JSONObject(json);
            u.setUserID(jObject.getLong("userID"));
            u.setFirstName(jObject.getString("firstName"));
            u.setLastName(jObject.getString("lastName"));
            u.setEmail(jObject.getString("email"));
            u.setPhone(jObject.getString("phoneNumber"));
            u.setPassword(jObject.getString("password"));
            u.setDisplayPicURL(jObject.getString("displayPicURL"));

        }catch (Exception ex){
            Log.d("ERROR",ex.toString());
            Log.d("ERROR",json);
        }
        return u;
    }

    public boolean isUserLoggedIn(){
        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        long userID = sharedPref.getLong("userID", 0);
        boolean loggedIn =  (userID == 0) ? false : true;
        return loggedIn;
    }

    public User getLoggedInUser(){//ALL THAT MATTERS IS THE USERID, NONE OF THE OTHER  INFO IS USED
        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        User u = new User();
        u.setUserID(sharedPref.getLong("userID",0));
        u.setFirstName(sharedPref.getString("firstName",""));
        u.setLastName(sharedPref.getString("lastName",""));
        u.setEmail(sharedPref.getString("email",""));
        u.setPhone(sharedPref.getString("phoneNumber",""));
        u.setDisplayPicURL(sharedPref.getString("displayPicURL",""));
        return u;
    }

    public void updateLoggedInUserOnPhone(User u){
        SharedPreferences.Editor sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();

        sharedPref.putLong("userID", u.getUserID());
        sharedPref.putString("firstName",u.getFirstName());
        sharedPref.putString("lastName",u.getLastName());
        sharedPref.putString("email",u.getEmail());
        sharedPref.putString("phoneNumber",u.getPhone());
        sharedPref.putString("password",u.getPassword());
        sharedPref.putString("displayPicURL",u.getDisplayPicURL());
        sharedPref.commit();
    }

    public void Logout(){
        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean terms = sharedPref.getBoolean("acceptedTerms", false);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.clear();
        edit.putBoolean("acceptedTerms", terms);//don't want to make user re-accept terms
        edit.commit();

        ConversationUpdateService.cancelAll(c);
        DatabaseHelper dbHelper = new DatabaseHelper(c);
        dbHelper.clearMessages();
        if(!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(c.getApplicationContext());
        }

        LoginManager.getInstance().logOut();

        ((Activity) c).recreate();
    }

    public void getUserDetailsRequest(final HttpResponseListener getUserDetailsResponse, final long userID){
        getUserDetailsResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_user_details.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getUserDetailsResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getUserDetailsResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("userID", userID + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void updateUserContactInfoRequest(final HttpResponseListener responseListener, final User u){
        responseListener.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/update_user_contact_info.php", new Response.Listener<String>() {
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
                params.put("userID", u.getUserID() + "");
                params.put("firstName", u.getFirstName());
                params.put("lastName", u.getLastName());
                params.put("phoneNumber", u.getPhone());
                params.put("email", u.getEmail());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void updateUserPasswordRequest(final HttpResponseListener responseListener, final long userID, final String password){
        responseListener.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/update_user_details.php", new Response.Listener<String>() {
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
                params.put("userID", userID + "");
                params.put("password", password);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void getUserContactInfoRequest(final HttpResponseListener getUserContactInfoResponse, final long userID){
        getUserContactInfoResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_user_contact_info.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getUserContactInfoResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getUserContactInfoResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("userID", userID + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void CreateAccountRequest(final HttpResponseListener mPostCommentResponse, final String firstName, final String lastName, final String emailPhone, final String password){
        mPostCommentResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/create_account.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("-1") || response.equals("0")){
                    mPostCommentResponse.requestCompleted(response);
                }else{
                    JSONObject jObjectData = new JSONObject();
                    try {
                        String email = "";
                        String phone = "";
                        if(emailPhone.contains("@")){
                            email = emailPhone;
                        }else{
                            phone = emailPhone;
                        }
                        jObjectData.put("userID", response);
                        jObjectData.put("firstName", firstName);
                        jObjectData.put("lastName", lastName);
                        jObjectData.put("email", email);
                        jObjectData.put("phoneNumber", phone);
                        jObjectData.put("password",password);
                        jObjectData.put("displayPicURL", "");

                    }catch (Exception ex){
                        Log.d("CREATEACCOUNT_REQUEST", ex.toString());
                    }
                    mPostCommentResponse.requestCompleted(jObjectData.toString());
                }
            }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mPostCommentResponse.requestEndedWithError(error);
                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("firstname",firstName);
                    params.put("lastname",lastName);
                    params.put("emailPhone", emailPhone);
                    params.put("password",password);

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                    return params;
                }
            };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void CreateOrLoginFacebookAccountRequest(final HttpResponseListener mPostCommentResponse, final String firstName, final String lastName, final String email, final String facebookID){
        mPostCommentResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/facebook_create_account_and_login.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mPostCommentResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPostCommentResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("email", email);
                params.put("facebookID", facebookID);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void CreateOrLoginGoogleAccountRequest(final HttpResponseListener mPostCommentResponse, final String firstName, final String lastName, final String email, final String displayPicURL, final String googleID){
        mPostCommentResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/google_create_account_and_login.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mPostCommentResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPostCommentResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("email", email);
                params.put("displayPicURL", displayPicURL);
                params.put("googleID", googleID);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void LoginRequest(final HttpResponseListener mHttpLoginRequestResponse, final String emailPhone, final String password, final boolean retrieveUserInfo){
        mHttpLoginRequestResponse.requestStarted();
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/login_check.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mHttpLoginRequestResponse.requestCompleted(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mHttpLoginRequestResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("emailPhone", emailPhone);
                params.put("password",password);
                params.put("retrieveUserInfo", String.valueOf(retrieveUserInfo));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put(c.getResources().getString(R.string.headerName), Security.encrypt(c.getResources().getString(R.string.code),c.getResources().getString(R.string.key)));
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

}
