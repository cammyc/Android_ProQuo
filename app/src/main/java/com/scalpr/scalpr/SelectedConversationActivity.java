package com.scalpr.scalpr;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.scalpr.scalpr.Adapters.ConversationListAdapter;
import com.scalpr.scalpr.Adapters.MessageListAdapter;
import com.scalpr.scalpr.Helpers.ConversationHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Conversation;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.Message;
import com.scalpr.scalpr.Objects.User;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class SelectedConversationActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    Button bSendMessage;
    EditText etMessageText;
    LinearLayout llFailedToConnect;

    boolean messagesAreLoaded = false;
    ArrayList<Message> messages;
    long lastMessageID = 0;
    //boolean cancelNewMesages = false;
    StringRequest currentGetNewMessagesRequest;

    Runnable newMessagesRunnable;
    Handler delayHandler;

    ConversationHelper convoHelper;
    DatabaseHelper dbHelper;
    HttpResponseListener getInitialMessagesListener, sendMessageListener, getNewMessagesListener, updateLastMessageReadListener;
    User me;
    Timer timer;

    Conversation convo;
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_conversation);
        c = this;
        messages = new ArrayList<Message>();

        me = new UserHelper(c).getLoggedInUser();

        convoHelper = new ConversationHelper(this);
        dbHelper = new DatabaseHelper(this);

        Bundle extras = getIntent().getExtras().getBundle("convoBundle");
        convo = (Conversation) extras.getSerializable("convo");

        delayHandler = new Handler();
        newMessagesRunnable = new Runnable() {
            @Override
            public void run() {
//                if(!cancelNewMesages){
                    currentGetNewMessagesRequest = convoHelper.getNewConversationMessagesRequest(getNewMessagesListener, convo.getID(), me.getUserID());
                //}
            }
        };

        llFailedToConnect = (LinearLayout) findViewById(R.id.llFailedToConnect);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvMessages);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle(convo.getTitle());

        LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        final View customActionBarView = inflater.inflate(R.layout.actionbar_selected_conversation, null);

        ImageView ivActionBarImage = (ImageView) customActionBarView.findViewById(R.id.ivActionBar);
        TextView tvActionBar = (TextView) customActionBarView.findViewById(R.id.tvActionBar);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.RIGHT));
        actionBar.setDisplayShowCustomEnabled(true);
        Glide.with(c).load(convo.getAttractionImageURL()).asBitmap().transform(new CropCircleTransformation(c)).dontAnimate().into(ivActionBarImage);//chaning image to prof pic eventually
        String title = (convo.getBuyerID() == me.getUserID()) ? convo.getSellerName() : convo.getBuyerName();
        tvActionBar.setText(title);

        Toolbar toolbar=(Toolbar) customActionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0,0);

        getInitialMessagesListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
                llFailedToConnect.setVisibility(View.GONE);
                messages = convoHelper.parseMessagesFromJSON(response);
                if(messages.size() > 0){
                    convoHelper.updateLastMessageRead(updateLastMessageReadListener, convo.getID(), me.getUserID(), messages.get(0).getID());
                }

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        dbHelper.addMessagesToDB(messages);
                    }
                });

               initializeInitialMessages(true);
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                llFailedToConnect.setVisibility(View.VISIBLE);
                getNewMessagesDelayed();

                Log.d("MESSAGES_ERROR", error.toString());
            }
        };

        bSendMessage = (Button) findViewById(R.id.bSendMessage);
        etMessageText = (EditText) findViewById(R.id.etMessageText);

        sendMessageListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
                long intResponse = Long.parseLong(response);
                for(int i = 0; i < messages.size(); i++){
                    long tempID = messages.get(i).getID();
                    if(tempID == -2){
                        if (intResponse == -1){
                            messages.remove(i);//there was an error putting message in database
                            Toast.makeText(c, "Unable to send message. Please try again.", Toast.LENGTH_SHORT).show();
                        }else{
                            Message m1 = messages.get(i);
                            Message m2 = messages.get(i+1);

                            Date date1 = m1.getTimeStamp();
                            Date date2 = m2.getTimeStamp();

                            long diff = date1.getTime() - date2.getTime();
                            long seconds = diff / 1000;
                            long minutes = seconds / 60;

                            lastMessageID = intResponse;

                            messages.get(i).setID(lastMessageID); //success
                            dbHelper.addMessageToDB(messages.get(i));//no need for async task here, quick function

                            if(minutes >= 30) {
                                Message timeBreak = new Message();
                                timeBreak.setID(-3);
                                timeBreak.setSenderID(-3);
                                timeBreak.setTimeStamp(date1);
                                messages.add(i+1, timeBreak); //add timebreak right above message sent
                                mAdapter.notifyItemInserted(i+1);
                            }

                            mAdapter.notifyItemChanged(i);

                            SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);//background service that checks for new messages is dependent on this being updated
                            long messageID = sharedPref.getLong("lastMessageID", 0);
                            if(lastMessageID > messageID){
                                sharedPref.edit().putLong("lastMessageID", lastMessageID).commit();
                               //php script only gets messages from other user, so no need for this - convoHelper.updateLastMessageRead(updateLastMessageSentListener, convo.getID(), me.getUserID(),lastMessageID);
                                // will need a token system if ever want people to message from multiple devices
                            }

                        }
                        break;
                    }
                }

                //cancelNewMesages = false;
                getNewMessagesDelayed();

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                for(int i = 0; i < messages.size(); i++){
                    long tempID = messages.get(i).getID();
                    if(tempID == -2){
                        //etMessageText.setText(messages.get(i).getText());
                        messages.remove(i);
                        mAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
                Toast.makeText(c, "Unable to send message. Please try again.", Toast.LENGTH_SHORT).show();
                Log.d("SENDMESSAGE", error.toString());

                currentGetNewMessagesRequest = convoHelper.getNewConversationMessagesRequest(getNewMessagesListener, convo.getID(), me.getUserID());
            }
        };

        bSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etMessageText.getText().toString();
                if (!text.trim().equals("") && messagesAreLoaded){
                    Message m = new Message();
                    m.setID(-2);// -2 means there should be a loading dialog
                    m.setText(text);
                    m.setSenderID(me.getUserID());
                    m.setConversationID(convo.getID());
                    m.setTimeStamp(new Date());

                    messages.add(0, m);
                    mAdapter.notifyItemInserted(0);
                    mLayoutManager.scrollToPosition(0);
                    etMessageText.setText("");

                    //need to cancel handler here
                    cancelNewMessages();
                    convoHelper.sendConversationMessageRequest(sendMessageListener, convo.getID(), me.getUserID(), text);
                }
            }
        });

        updateLastMessageReadListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {

            }

            @Override
            public void requestEndedWithError(VolleyError error) {

            }
        };

        getNewMessagesListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
                llFailedToConnect.setVisibility(View.GONE);
                final ArrayList<Message> newMessages = convoHelper.parseMessagesFromJSON(response);//parseMessagesFromJSON updates Shared prefs

                if(newMessages.size() > 0 && newMessages.get(0).getID() != lastMessageID) { //second part is in case message was sent and then received by getNewMessage - avoid duplicate
                    convoHelper.updateLastMessageRead(updateLastMessageReadListener, convo.getID(), me.getUserID(), newMessages.get(0).getID());

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            dbHelper.addMessagesToDB(newMessages);
                        }
                    });
                    messages.addAll(0,newMessages);

                    int range = newMessages.size();

                    if(messages.size() > 1) {//will crash if messages.size isn't greater than 1
                        for (int i = 0; i < newMessages.size(); i++) {
                            Date date1 = messages.get(i).getTimeStamp();
                            Date date2 = messages.get(i + 1).getTimeStamp();

                            long diff = date1.getTime() - date2.getTime();
                            long seconds = diff / 1000;
                            long minutes = seconds / 60;

                            if (minutes >= 30) {
                                Message m = new Message();
                                m.setID(-3);
                                m.setSenderID(-3);//determines view type, anything below 0 for sender ID means not a message, -3 means timebreak, trying to keep consistent with ID code;
                                m.setTimeStamp(date1);//date1 because messages array list is in reverse order, date 1 is actually the more recent message
                                messages.add(i + 1, m);
                                i++;//double increment because adding to list
                                range++;
                            }
                        }
                    }

                    lastMessageID = messages.get(0).getID();
                    mAdapter.notifyItemRangeInserted(0, range);
                    mLayoutManager.scrollToPosition(0);
                }

                getNewMessagesDelayed();
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
               // if(!cancelNewMesages) {
                    llFailedToConnect.setVisibility(View.VISIBLE);
                    getNewMessagesDelayed();
                //}
            }
        };

        messages = dbHelper.getMessagesFromDB(convo.getID(),0);
        if(messages.size() > 0){
            Log.d("HELLO", messages.size() + "");
            initializeInitialMessages(false);
        }else{
            convoHelper.getConversationMessagesRequest(getInitialMessagesListener,convo.getID());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelNewMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(cancelNewMesages){
//            cancelNewMesages = false;
            getNewMessagesDelayed();
       // }
    }

    private void getNewMessagesDelayed(){//because this is delayed when the queue is canceled, by luck, this runs 2 seconds later and restarts the queue. That won't work on crappy connection where request takes longer than 2 seconds
        //so what needs to happen is when I cancel the quese I cancel the handler. Then when the message is sent I restart the handler
        delayHandler.postDelayed(newMessagesRunnable, 2000);//this may feel laggy, will switch to 1 second after testing
    }

    private void initializeInitialMessages(boolean delay){
        for(int i = 0; i < messages.size()-1; i++){
            Date date1 = messages.get(i).getTimeStamp();
            Date date2 = messages.get(i + 1).getTimeStamp();

            long diff = date1.getTime() - date2.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;

            if(minutes >= 30){
                Message m = new Message();
                m.setID(-3);
                m.setSenderID(-3);//determines view type, anything below 0 for sender ID means not a message, -3 means timebreak, trying to keep consistent with ID code;
                m.setTimeStamp(date1);//date1 because messages array list is in reverse order, date 1 is actually the more recent message
                messages.add(i + 1, m);
                i++;//double increment because adding to list
            }
        }

        Message m = new Message();
        m.setID(-3);
        m.setSenderID(-3);
        m.setTimeStamp(convo.getCreationTimeStamp());
        messages.add(m);//ading conversation creation date timestamp

        lastMessageID = messages.get(0).getID();

        mAdapter = new MessageListAdapter(c, messages);
        mRecyclerView.setAdapter(mAdapter);
        messagesAreLoaded = true;

        if(delay){
            getNewMessagesDelayed();
        }else{
            newMessagesRunnable.run(); //no need for delay on first call.
        }
    }

    private void cancelNewMessages(){
        //cancelNewMesages = true;

        if(convoHelper != null) {
            convoHelper.cancelRequests(); //VERY IMPORTANT this is in case a getNewMessagesRequest is ahead on the queue.Trying to avoid duplicates messages showing up
        }

        if(delayHandler != null){
            delayHandler.removeCallbacks(newMessagesRunnable);
        }
    }


}
