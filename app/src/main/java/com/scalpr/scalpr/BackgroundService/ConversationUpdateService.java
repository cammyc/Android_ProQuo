package com.scalpr.scalpr.BackgroundService;

/**
 * Created by Cam on 11/19/2016.
 */
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.scalpr.scalpr.ConversationsActivity;
import com.scalpr.scalpr.EditAccountDetailsActivity;
import com.scalpr.scalpr.Helpers.ConversationHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.MainActivity;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.Message;
import com.scalpr.scalpr.Objects.NotificationMessage;
import com.scalpr.scalpr.R;

import java.util.ArrayList;
import java.util.Random;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ConversationUpdateService extends GcmTaskService {

    private static final String TAG = ConversationUpdateService.class.getSimpleName();

    public static final String GCM_ONEOFF_TAG = "oneoff|[0,0]";
    public static final String GCM_REPEAT_TAG = "repeat|[7200,1800]";

    @Override
    public void onInitializeTasks() {
        //called when app is updated to a new version, reinstalled etc.
        //you have to schedule your repeating tasks again
        super.onInitializeTasks();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        //do some stuff (mostly network) - executed in background thread (async)

        //obtain your data
        //Bundle extras = taskParams.getExtras();

        Handler h = new Handler(getMainLooper());
        Log.v(TAG, "onRunTask");//THIS ISN'T WORKING WHEN USER LOG'S IN AND lastMessageID is 0

        h.post(new Runnable() {
            @Override
            public void run() {

                final Context c = (ConversationUpdateService.this);
                final ConversationHelper convoHelper = new ConversationHelper(c);

                final long userID = new UserHelper(c).getLoggedInUser().getUserID();
                Log.v(TAG, "onRunTask - " + userID);

                final HttpResponseListener notificationListener = new HttpResponseListener() {
                    @Override
                    public void requestStarted() {

                    }

                    @Override
                    public void requestCompleted(String response) {
                        Log.v(TAG, "onRunTask - NotificaitonResponse - " + response);
                    }

                    @Override
                    public void requestEndedWithError(VolleyError error) {

                    }
                };

                HttpResponseListener listener = new HttpResponseListener() {
                    @Override
                    public void requestStarted() {

                    }

                    @Override
                    public void requestCompleted(String response) {
                        Log.v(TAG, "onRunTaskresponse - " + response);
                        if(!response.equals("0")){
                            final ArrayList<NotificationMessage> notMesList = convoHelper.parseNotificationMessageFromJSON(response);

                            // Creates an explicit intent for an Activity in your app
                            Intent resultIntent = new Intent(ConversationUpdateService.this, ConversationsActivity.class);

                            // The stack builder object will contain an artificial back stack for the
                            // started Activity.
                            // This ensures that navigating backward from the Activity leads out of
                            // your application to the Home screen.
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ConversationUpdateService.this);
                            // Adds the back stack for the Intent (but not the Intent itself)
                            stackBuilder.addParentStack(ConversationsActivity.class);
                            // Adds the Intent that starts the Activity to the top of the stack
                            stackBuilder.addNextIntent(resultIntent);
                            final PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                            Bitmap b = null;

                            int widthHeight = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);

                            /* below is ugly - comments to explain */

                            //first off I needed a way of updating the conversation specific notification when it changed not initially
                            //so below i am initializing everything before the loop starts

                            for(int i = 0; i<notMesList.size(); i++){
                                final NotificationMessage notMes = notMesList.get(i);
                                convoHelper.updateLastNotificationReceived(notificationListener, notMes.getConvoID(), userID, notMes.getMessageID());
                                final NotificationCompat.Builder notification = buildNotificationWithoutImage(notMesList.get(0), resultPendingIntent);//get first message

                                Glide.with(c).load(notMes.getImageURL()).asBitmap().transform(new CropCircleTransformation(c)).dontAnimate().listener(new RequestListener<String, Bitmap>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                        notification.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher2));
                                        NotificationManagerCompat.from(ConversationUpdateService.this).notify(notMes.getConvoID() + "", 0, notification.build());
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                        notification.setLargeIcon(resource);
                                        NotificationManagerCompat.from(ConversationUpdateService.this).notify(notMes.getConvoID() + "", 0, notification.build());
                                        return false;
                                    }
                                }).into(widthHeight, widthHeight);

                            }

                        }
                    }

                    @Override
                    public void requestEndedWithError(VolleyError error) {
                        Log.v(TAG, "onRunTask - " + error.toString());
                    }
                };

                convoHelper.backgroundCheckNewConversationMessagesRequest(listener, userID);
            }
        });

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void scheduleRepeat(Context context) {
//        //in this method, single Repeating task is scheduled (the target service that will be called is ConversationUpdateService.class)
//        try {
//            PeriodicTask periodic = new PeriodicTask.Builder()
//                    //specify target service - must extend GcmTaskService
//                    .setService(ConversationUpdateService.class)
//                    //repeat every 10 seconds
//                    .setPeriod(10)
//                    //specify how much earlier the task can be executed (in seconds)
//                    .setFlex(10)
//                    //tag that is unique to this task (can be used to cancel task)
//                    .setTag(GCM_REPEAT_TAG)
//                    //whether the task persists after device reboot
//                    .setPersisted(true)
//                    //if another task with same tag is already scheduled, replace it with this task
//                    .setUpdateCurrent(true)
//                    //set required network state, this line is optional
//                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
//                    //request that charging must be connected, this line is optional
//                    .setRequiresCharging(false)
//                    .build();
//            GcmNetworkManager.getInstance(context).schedule(periodic);
//            Log.v(TAG, "repeating task scheduled");
//        } catch (Exception e) {
//            Log.e(TAG, "scheduling failed");
//            e.printStackTrace();
//        }
    }

    private NotificationCompat.Builder buildNotificationWithoutImage(NotificationMessage notMes, PendingIntent resultPendingIntent){

        return new NotificationCompat.Builder(ConversationUpdateService.this)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(notMes.getYourName())
                .setContentText(notMes.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notMes.getMessage()))
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.ic_proquo_notification_2)
                .setShowWhen(true)
                .setColor(Color.parseColor("#2ecc71"))
                .setLocalOnly(true)
                //.setOnlyAlertOnce(true) does nothing
                .setVibrate(new long[] { 1000, 1000})
                .setLights(Color.parseColor("#2ecc71"), 2000, 2000)
                .setPriority(Notification.PRIORITY_HIGH);

    }

//    public static void cancelOneOff(Context context) {
//        GcmNetworkManager
//                .getInstance(context)
//                .cancelTask(GCM_ONEOFF_TAG, ConversationUpdateService.class);
//    }
//
//    public static void cancelRepeat(Context context) {
//        GcmNetworkManager
//                .getInstance(context)
//                .cancelTask(GCM_REPEAT_TAG, ConversationUpdateService.class);
//    }

    public static void cancelAll(Context context) {
        GcmNetworkManager
                .getInstance(context)
                .cancelAllTasks(ConversationUpdateService.class);
    }

}