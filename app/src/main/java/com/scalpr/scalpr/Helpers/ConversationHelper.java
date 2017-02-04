package com.scalpr.scalpr.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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
import com.scalpr.scalpr.Objects.Conversation;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.Message;
import com.scalpr.scalpr.Objects.NotificationMessage;
import com.scalpr.scalpr.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cam on 11/6/2016.
 */
public class ConversationHelper {
    Context c;
    RequestQueue queue;
    String TAG = "CONVO_HELPER_DEFAULT";
    String TAG_LAST_READ = "UPDATE_LAST_READ";
    public ConversationHelper(Context _c){
        c = _c;
        queue = Volley.newRequestQueue(c);
    }

    public void createConversationRequest(final HttpResponseListener responseListener, final long attractionID, final long buyerID, final String attractionName){
        responseListener.requestStarted();
        
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/create_conversation.php", new Response.Listener<String>() {
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
                params.put("attractionID", attractionID + "");
                params.put("buyerID",buyerID + "");
                params.put("attractionName", attractionName);
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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void getUserConversationsRequest(final HttpResponseListener responseListener, final long userID){//convo doesn't need title - just get other persons name
        responseListener.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_user_conversations.php", new Response.Listener<String>() {
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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0, //DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR in the app. in the app. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void getConversationMessagesRequest(final HttpResponseListener responseListener, final long conversationID){
        responseListener.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_conversation_messages.php", new Response.Listener<String>() {
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
                params.put("conversationID", conversationID + "");

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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//already have getNewMessages called if this fails - //DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public StringRequest getNewConversationMessagesRequest(final HttpResponseListener responseListener, final long conversationID, final long userID){
        responseListener.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/get_new_conversation_messages.php", new Response.Listener<String>() {
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
                params.put("conversationID", conversationID + "");
                params.put("userID", userID + "");

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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//it autoloops on its own - //DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
        return sr;
    }

    public StringRequest backgroundCheckNewConversationMessagesRequest(final HttpResponseListener responseListener, final long userID){
        responseListener.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/check_new_conversation_messages.php", new Response.Listener<String>() {
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

                try{
                    PackageInfo packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
                    double currentVersion = Double.parseDouble(packageInfo.versionName);

                    params.put("appVersion", currentVersion + "");
                    params.put("appType", 1 + ""); //1 is android
                }catch (Exception ex){
                    Log.d("VALIDATE_VERSION", ex.toString());
                }


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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//it autoloops on its own - //DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
        return sr;
    }

    public ArrayList<Conversation> parseConversationsFromJSON(String json){
        ArrayList<Conversation> conversations = new ArrayList<Conversation>();

        try{
            JSONArray jArray = new JSONArray(json);

            for(int i = 0; i < jArray.length(); i++){
                Conversation convo = new Conversation();
                JSONObject jsonConvo = jArray.getJSONObject(i);

                convo.setID(jsonConvo.getLong("conversationID"));
                convo.setAttractionID(jsonConvo.getLong("attractionID"));
                convo.setBuyerID(jsonConvo.getLong("buyerID"));
                convo.setSellerID(jsonConvo.getLong("sellerID"));
                convo.setBuyerName(jsonConvo.getString("buyerName"));
                convo.setSellerName(jsonConvo.getString("sellerName"));
                convo.setTitle(jsonConvo.getString("title"));

                JSONObject jsonMessage = jsonConvo.getJSONObject("lastMessage");
                if(!jsonMessage.getString("messageID").equals("null")){
                    Message m = new Message();
                    m.setID(jsonMessage.getLong("messageID"));
                    m.setConversationID(jsonMessage.getLong("conversationID"));
                    m.setSenderID(jsonMessage.getLong("senderID"));
                    m.setText(jsonMessage.getString("message"));
                    m.setTimeStamp(MiscHelper.parseUTCStringToDate(jsonMessage.getString("timestamp"),"yyyy-MM-dd H:m:s"));

                    SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);// the shared prefs is not needed anymore, the text to the right is false now I belive ->  background service that checks for new messages is dependent on this being updated;
                    long messageID = sharedPref.getLong("lastMessageID", 0);
                    if(m.getID() > messageID){
                       sharedPref.edit().putLong("lastMessageID", m.getID()).commit();
                    }

                    convo.setLastMessage(m);
                }
                convo.setAttractionImageURL(jsonConvo.getString("attractionImageURL"));
                convo.setCreationTimeStamp(MiscHelper.parseUTCStringToDate(jsonConvo.getString("creationTimestamp"),"yyyy-MM-dd H:m:s"));
                convo.setLastMessageRead((jsonConvo.getInt("isLastMessageRead") == 0) ? false : true);

                conversations.add(convo);
            }

        }catch (Exception ex){
            Log.d("CONVERSATION_JSON_ERROR",ex.toString());
        }

        return conversations;
    }

    public NotificationMessage parseSingleNotificationMessageFromJSON(String json){
        NotificationMessage notMes = new NotificationMessage();

        try {
            JSONObject jsonMesNot = new JSONObject(json);

                notMes.setMessageID(jsonMesNot.getLong("messageID"));
                notMes.setMessage(jsonMesNot.getString("message"));
                notMes.setConvoID(jsonMesNot.getLong("convoID"));
                notMes.setYourName(jsonMesNot.getString("yourName"));
                notMes.setImageURL(jsonMesNot.getString("imageURL"));
                notMes.setAttractionName(jsonMesNot.getString("attractionName"));

        }catch (Exception ex){
            return null;
        }
        return notMes;
    }

    public ArrayList<NotificationMessage> parseNotificationMessageFromJSON(String json){
        ArrayList<NotificationMessage> list = new ArrayList<NotificationMessage>();
        try{
            JSONArray jArray = new JSONArray(json);
            for(int i = 0; i < jArray.length(); i++) {
                NotificationMessage notMes = new NotificationMessage();
                JSONObject jsonMesNot = jArray.getJSONObject(i);

                notMes.setMessageID(jsonMesNot.getLong("messageID"));
                notMes.setMessage(jsonMesNot.getString("message"));
                notMes.setConvoID(jsonMesNot.getLong("convoID"));
                notMes.setYourName(jsonMesNot.getString("yourName"));
                notMes.setImageURL(jsonMesNot.getString("imageURL"));
                notMes.setAttractionName(jsonMesNot.getString("attractionName"));

                list.add(notMes);
            }

            if(list.size() > 0){
                SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);//background service that checks for new messages is dependent on this being updated;
                long messageID = sharedPref.getLong("lastMessageID", 0);
                if (list.get(0).getMessageID() > messageID) {
                    sharedPref.edit().putLong("lastMessageID", list.get(0).getMessageID()).commit();
                }
            }

        }catch (Exception ex){
            Log.d("CONVERSATION_JSON_ERROR",ex.toString());
        }

        return list;
    }

    public void sendConversationMessageRequest(final HttpResponseListener getUserAttractionsResponse, final long conversationID, final long senderID, final String message){
        getUserAttractionsResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/send_conversation_message.php", new Response.Listener<String>() {
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
                params.put("conversationID",conversationID + "");
                params.put("senderID", senderID + "");
                params.put("message", message);
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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        sr.setRetryPolicy(new DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //this congif makes ui thread very glitchy
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void updateLastMessageRead(final HttpResponseListener getUserAttractionsResponse, final long conversationID, final long userID, final long messageID){
        getUserAttractionsResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/update_user_last_read_message_for_conversation.php", new Response.Listener<String>() {
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
                params.put("messageID", messageID + "");
                params.put("conversationID",conversationID + "");
                params.put("userID", userID + "");
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
        sr.setTag(TAG_LAST_READ);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        sr.setRetryPolicy(new DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //this congif makes ui thread very glitchy
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void updateLastNotificationReceived(final HttpResponseListener getUserAttractionsResponse, final long conversationID, final long userID, final long messageID){
        getUserAttractionsResponse.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/update_user_last_notified_message_for_conversation.php", new Response.Listener<String>() {
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
                params.put("messageID", messageID + "");
                params.put("conversationID",conversationID + "");
                params.put("userID", userID + "");
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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        sr.setRetryPolicy(new DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, //this congif makes ui thread very glitchy
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public StringRequest userLeaveConversation(final HttpResponseListener responseListener, final long conversationID, final long userID){
        responseListener.requestStarted();
        StringRequest sr = new StringRequest(Request.Method.POST,"https://scalpr-143904.appspot.com/scalpr_ws/user_leave_conversation.php", new Response.Listener<String>() {
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
                params.put("conversationID", conversationID + "");
                params.put("userID", userID + "");

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
        sr.setTag(TAG);
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                0,//it autoloops on its own - //DONT FUCK WITH THE REPEATING. DONT WANT ANY REPEAT OR THERE MAY BE REPETITION IN THE DATABASE OR IN THE APP. KEEP IT AT 0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
        return sr;
    }

    public ArrayList<Message> parseMessagesFromJSON(String json){
        ArrayList<Message> messages = new ArrayList<Message>();

        try{
            JSONArray jArray = new JSONArray(json);
            for(int i = 0; i < jArray.length(); i++) {
                Message m = new Message();
                JSONObject jsonMessage = jArray.getJSONObject(i);
                m.setID(jsonMessage.getLong("messageID"));
                m.setConversationID(jsonMessage.getLong("conversationID"));
                m.setSenderID(jsonMessage.getLong("senderID"));
                m.setText(jsonMessage.getString("message"));
                m.setTimeStamp(MiscHelper.parseUTCStringToDate(jsonMessage.getString("timestamp"),"yyyy-MM-dd H:m:s"));

                messages.add(m);
            }
        }catch (Exception ex){
            Log.d("CONVERSATION_JSON_ERROR",ex.toString());
        }

        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);//background service that checks for new messages is dependent on this being updated
        long messageID = sharedPref.getLong("lastMessageID", 0);
        if(messages.size() > 0){
            if(messages.get(0).getID() > messageID){
                sharedPref.edit().putLong("lastMessageID", messages.get(0).getID()).commit();
            }
        }


        return messages;
    }

    public void cancelRequests(){
        queue.cancelAll(TAG);
    }

    public RequestQueue getQueue(){
        return queue;
    }
}
