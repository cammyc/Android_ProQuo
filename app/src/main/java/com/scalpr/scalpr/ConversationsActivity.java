package com.scalpr.scalpr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.scalpr.scalpr.Adapters.ConversationListAdapter;
import com.scalpr.scalpr.Helpers.ConversationHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.DbInitializer;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.Conversation;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.User;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ConversationsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout llLoadConvosFailed;
    Button bTryAgain;
    private ConversationHelper convoHelper;
    private UserHelper userHelper;
    private ProgressBar pbConvoLoader;
    private TextView tvNoConversations;

    HttpResponseListener responseListener, updateConvoResponseListener, checkVersionListener;

    Context context;

    Runnable newMessagesRunnable;
    //Handler delayHandler;
   // boolean cancelNewMessages = false;

    User me;

    ArrayList<Conversation> conversations;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    boolean dontUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        context = this;

        convoHelper = new ConversationHelper(this);
        userHelper = new UserHelper(this);

        llLoadConvosFailed = (LinearLayout) findViewById(R.id.llLoadConversationsFailed);
        bTryAgain = (Button) findViewById(R.id.bTryLoadConversationsAgain);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvConversations);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        pbConvoLoader = (ProgressBar) findViewById(R.id.pbConvoLoader);

        tvNoConversations = (TextView) findViewById(R.id.tvNoConversations);

        me = userHelper.getLoggedInUser();

        checkMinimumVersion();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent intent) {
                //Toast.makeText(context, "eeeeeeh oh", Toast.LENGTH_SHORT).show();
                if (!dontUpdate){
                    newMessagesRunnable.run();
                }
            }
        };

        mIntentFilter=new IntentFilter("GET_NEW_MESSAGES");

        responseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                pbConvoLoader.setVisibility(View.VISIBLE);
                llLoadConvosFailed.setVisibility(View.GONE);
            }

            @Override
            public void requestCompleted(String response) {
                pbConvoLoader.setVisibility(View.GONE);
                if(!response.equals("0")) {
                    final ArrayList<Conversation> tempConvos = convoHelper.parseConversationsFromJSON(response);

                    if (tempConvos.size() > 0) {
                        conversations = tempConvos;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                deleteOldMessages(conversations);
                            }
                        });
                        mAdapter = new ConversationListAdapter(context, conversations);
                        mRecyclerView.setAdapter(mAdapter);

                        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                            @Override
                            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                return false;
                            }

                            @Override
                            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                                try{
                                    if (conversations.get(viewHolder.getAdapterPosition()).getID() <= 0)
                                        return 0;
                                }catch (Exception ex){
                                    return super.getSwipeDirs(recyclerView, viewHolder);
                                }

                                return super.getSwipeDirs(recyclerView, viewHolder);
                            }

                            @Override
                            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                                //cancelNewMessages(); //temporarily pause
                                dontUpdate = true;

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                                // set title
                                alertDialogBuilder.setTitle("Delete Ticket");

                                // set dialog message
                                alertDialogBuilder
                                        .setMessage("Are you sure you want to leave this conversation?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, int id) {
                                                final Conversation selectedConvo = conversations.get(viewHolder.getAdapterPosition());

                                                HttpResponseListener listener = new HttpResponseListener() {
                                                    @Override
                                                    public void requestStarted() {
                                                        pbConvoLoader.setVisibility(View.VISIBLE);
                                                    }

                                                    @Override
                                                    public void requestCompleted(String response) {
                                                        //cancelNewMessages = false;//need to set to true because new messages was canceled - this seems redundant not sure if needed...
                                                        //getNewMessagesDelayed();
                                                        dontUpdate = false;
                                                        if (response.equals("1")) {
                                                            conversations.remove(viewHolder.getAdapterPosition());
                                                            mRecyclerView.getAdapter().notifyDataSetChanged();
                                                            clearView(mRecyclerView, viewHolder);
                                                            dialog.dismiss();
                                                            //dbHelper.deleteAttraction(selectedAttr.getID());
                                                            Toast.makeText(context, "Successfully left conversation", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                                                            clearView(mRecyclerView, viewHolder);
                                                            dialog.dismiss();
                                                            Toast.makeText(context, "Unable to leave conversation. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                                                        }
                                                        pbConvoLoader.setVisibility(View.GONE);
                                                    }

                                                    @Override
                                                    public void requestEndedWithError(VolleyError error) {
                                                       // cancelNewMessages = false;
                                                        //getNewMessagesDelayed();
                                                        dontUpdate = false;
                                                        mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                                                        clearView(mRecyclerView, viewHolder);
                                                        dialog.dismiss();
                                                        Toast.makeText(context, "Unable to leave conversation. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                                                        pbConvoLoader.setVisibility(View.GONE);
                                                    }
                                                };

                                                convoHelper.userLeaveConversation(listener, selectedConvo.getID(), me.getUserID());
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                                                clearView(mRecyclerView, viewHolder);
                                                //cancelNewMessages = false;
                                                dontUpdate = false;
                                                //getNewMessagesDelayed();
                                                dialog.dismiss();
                                            }
                                        });

                                // create alert dialog
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                            }

                            @Override
                            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                                //Log.d("state", actionState + " - " + isCurrentlyActive);
                                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                    // Get RecyclerView item from the ViewHolder
                                    View itemView = viewHolder.itemView;

                                    Paint pBG = new Paint();
                                    pBG.setColor(Color.parseColor("#e74c3c"));

                                    Paint pWhite = new Paint();
                                    pWhite.setColor(Color.WHITE);
                                    pWhite.setTextSize(60);
                                    pWhite.setTypeface(Typeface.DEFAULT_BOLD);
                                    if (dX > 0) {
                                    /* Set your color for positive displacement */
                                        Rect r = new Rect(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
                                        // Draw Rect with varying right side, equal to displacement dX
                                        c.drawRoundRect(new RectF(r), MiscHelper.convertDpToPixel(5, context), MiscHelper.convertDpToPixel(5, context), pBG);
                                        c.drawRect(new RectF(r), pBG);

                                        float yPos = ((itemView.getHeight() / 2) - ((pWhite.descent() + pWhite.ascent()) / 2));

                                        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                                                R.mipmap.ic_circle_left);

                                        Bitmap scaled = Bitmap.createScaledBitmap(icon, (int) (icon.getWidth() * .66), (int) (icon.getHeight() * .66), true);

                                        icon.recycle();

                                        c.drawBitmap(scaled,
                                                (float) itemView.getLeft() + MiscHelper.convertDpToPixel(10, context),
                                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - scaled.getHeight()) / 2,
                                                pWhite);

                                        c.drawText("LEAVE CONVERSATION",
                                                (float) itemView.getLeft() + MiscHelper.convertDpToPixel(14, context) + scaled.getWidth(),
                                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - pWhite.ascent()) / 2,
                                                pWhite);

                                    }
                                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                                }
                            }

                            @Override
                            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                                Log.d("state", actionState + "");

                                if(actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                                    //getNewMessagesDelayed();
                                    dontUpdate = false;
                                    newMessagesRunnable.run();

                                }else if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                                    dontUpdate = true;
                                    //cancelNewMessages();
                                }

                                super.onSelectedChanged(viewHolder, actionState);
                            }
                        };

                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                        itemTouchHelper.attachToRecyclerView(mRecyclerView);

                        mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                return false;
                            }
                        });

                        for(int i = 0; i < conversations.size(); i++){
                            Conversation convo = conversations.get(i);
                            Bundle bConvoID = getIntent().getBundleExtra("convoIDBundle");

                            if(bConvoID != null){
                                long id = bConvoID.getLong("goToConvoID", 0);
                                if(id == convo.getID()){
                                    Intent intent = new Intent(context, SelectedConversationActivity.class);
                                    Bundle b = new Bundle();
                                    b.putSerializable("convo", convo);
                                    intent.putExtra("convoBundle", b);
                                    context.startActivity(intent);
                                    return;
                                }
                            }

                        }
                    } else {
                        tvNoConversations.setVisibility(View.VISIBLE);
                    }

                    //cancelNewMessages = false;
                   // getNewMessagesDelayed();
                }else{
                    llLoadConvosFailed.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                pbConvoLoader.setVisibility(View.GONE);
                llLoadConvosFailed.setVisibility(View.VISIBLE);
                Log.d("CONVERSATION", error.toString());
            }
        };

        updateConvoResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
                if (!response.equals("0")){
                    final ArrayList<Conversation> tempConvos = convoHelper.parseConversationsFromJSON(response);

                    if(tempConvos.size() > 0) {
                        conversations.clear();
                        conversations.addAll(tempConvos);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                //getNewMessagesDelayed();
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                pbConvoLoader.setVisibility(View.GONE);
                llLoadConvosFailed.setVisibility(View.VISIBLE);
                Log.d("CONVERSATION", error.toString());
            }
        };

        bTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convoHelper.getUserConversationsRequest(responseListener, me.getUserID());
            }
        });

        //delayHandler = new Handler();
        newMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                //if(!cancelNewMessages){
                    convoHelper.getUserConversationsRequest(updateConvoResponseListener, userHelper.getLoggedInUser().getUserID());
                //}
            }
        };

        if(userHelper.isUserLoggedIn()) {
            convoHelper.getUserConversationsRequest(responseListener, userHelper.getLoggedInUser().getUserID());
        }else{
            finish();
        }

    }

//    private void getNewMessagesDelayed(){//because this is delayed when the queue is canceled, by luck, this runs 2 seconds later and restarts the queue. That won't work on crappy connection where request takes longer than 2 seconds
//        //so what needs to happen is when I cancel the quese I cancel the handler. Then when the message is sent I restart the handler
//        delayHandler.postDelayed(newMessagesRunnable, 4000);//this may feel laggy, will switch to 1 second after testing
//    }
//
//    private void cancelNewMessages(){
//        //cancelNewMessages = true;
//
//        if(convoHelper != null) {
//            convoHelper.cancelRequests(); //VERY IMPORTANT this is in case a getNewMessagesRequest is ahead on the queue.Trying to avoid duplicates messages showing up
//        }
//
//        if(delayHandler != null){
//            delayHandler.removeCallbacks(newMessagesRunnable);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        dontUpdate = false;
        MiscHelper.showNotification = false;
        registerReceiver(mReceiver, mIntentFilter);
        if(userHelper.isUserLoggedIn()) {
            if(mAdapter != null){
//                conversations = new ArrayList<Conversation>();
//                mAdapter.notifyDataSetChanged();
//                convoHelper.getUserConversationsRequest(responseListener, userHelper.getLoggedInUser().getUserID());
                newMessagesRunnable.run();
            }
        }else{
            finish();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        dontUpdate = true;
        MiscHelper.showNotification = true;
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        //cancelNewMessages();
    }

    private void deleteOldMessages(ArrayList<Conversation> convos){
        String whereClause = "";
        for(int i = 0; i < convos.size(); i++){
            whereClause += DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID + " != " + convos.get(i).getID();
            if(i != convos.size()-1){
                whereClause += " AND ";
            }
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        boolean success = dbHelper.deleteMessagesFromConversation(whereClause);
    }


    private void checkMinimumVersion(){
        checkVersionListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
            }

            @Override
            public void requestCompleted(String response) {
                try{
                    double minVersion = Double.parseDouble(response);
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    double currentVersion = Double.parseDouble(packageInfo.versionName);

                    if(currentVersion < minVersion){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                context);

                        // set title
                        alertDialogBuilder.setTitle("Update Required");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("An update is required to continue using BeLive. Please close the app and update it in the Google Play Store.")
                                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                                .setCancelable(false);

                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();

                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }

                                alertDialog.dismiss();
                                finish();
                            }
                        });
                    }else{
                        terms();//no need for terms if app is to be updated
                    }

                }catch (Exception ex){
                    Log.d("MIN_VERSION", ex.toString());
                    terms();
                }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                terms();
            }
        };

        MiscHelper miscHelper = new MiscHelper();
        miscHelper.checkForUpdateRequest(checkVersionListener, context);

    }

    private void terms(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(!sharedPref.getBoolean("acceptedTerms", false)) {


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("Terms of Service");

            // set dialog message
            alertDialogBuilder
                    .setMessage("You must agree to BeLives Terms of Service to continue using the app.")
                    .setNegativeButton("View Terms of Service", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("I agree", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setCancelable(false);

            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                    sharedPref.putBoolean("acceptedTerms", true);
                    sharedPref.commit();
                    alertDialog.dismiss();
                }
            });

            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.belivetickets.com/help/policies/terms_of_service.html"));
                    startActivity(browserIntent);
                }
            });
        }
    }


}
