package com.scalpr.scalpr.BackgroundService;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.scalpr.scalpr.ConversationsActivity;
import com.scalpr.scalpr.Helpers.ConversationHelper;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.MainActivity;
import com.scalpr.scalpr.Objects.NotificationMessage;
import com.scalpr.scalpr.R;

import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();

            NotificationMessage notMes = new NotificationMessage();
            try{
                notMes.setMessageID(Long.parseLong(data.get("messageID")));
                notMes.setMessage(data.get("message"));
                notMes.setConvoID(Long.parseLong(data.get("convoID")));
                notMes.setYourName(data.get("yourName"));
                notMes.setImageURL(data.get("imageURL"));
                notMes.setAttractionName(data.get("attractionName"));

                if(MiscHelper.showNotification) {
                    sendNotification(notMes);
                }

                Intent broadcast = new Intent();
                broadcast.setAction("GET_NEW_MESSAGES");
                sendBroadcast(broadcast);

            }catch (Exception ex){

            }


        }


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private void sendNotification(final NotificationMessage notMes) {


            Intent intent = new Intent(this, ConversationsActivity.class);
            Bundle b = new Bundle();
            b.putLong("goToConvoID", notMes.getConvoID());
            intent.putExtra("convoIDBundle", b);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);


            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(notMes.getYourName())
                    .setContentText(notMes.getMessage())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notMes.getMessage()))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_proquo_notification_2)
                    .setShowWhen(true)
                    .setColor(Color.parseColor("#2ecc71"))
                    .setLocalOnly(true)
                    //.setOnlyAlertOnce(true) does nothing
                    .setVibrate(new long[]{1000, 1000})
                    .setLights(Color.parseColor("#2ecc71"), 2000, 2000)
                    .setPriority(Notification.PRIORITY_HIGH);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int widthHeight = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);
        final Context c = this;

                            /* below is ugly - comments to explain */

        //first off I needed a way of updating the conversation specific notification when it changed not initially
        //so below i am initializing everything before the loop starts

            Glide.with(c).load(notMes.getImageURL()).asBitmap().transform(new CropCircleTransformation(c)).dontAnimate().listener(new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher2));
                    NotificationManagerCompat.from(c).notify(notMes.getConvoID() + "", 0, notificationBuilder.build());
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                    notificationBuilder.setLargeIcon(resource);
                    NotificationManagerCompat.from(c).notify(notMes.getConvoID() + "", 0, notificationBuilder.build());
                    return false;
                }
            }).into(widthHeight, widthHeight);

    }
}