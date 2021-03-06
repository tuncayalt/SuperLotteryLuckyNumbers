package com.tuncay.superlotteryluckynumbers.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tuncay.superlotteryluckynumbers.R;
import com.tuncay.superlotteryluckynumbers.SavedActivity;

/**
 * Created by mac on 16.04.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Intent intent  = new Intent(this, SavedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("Super Loto Sansli Numaralar");
        notificationBuilder.setContentText(remoteMessage.getNotification().getBody());
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.mipmap.clover_logo);
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
