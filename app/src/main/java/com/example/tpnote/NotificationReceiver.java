package com.example.tpnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("notificationId", 0);

        // Utilisez NotificationHelper pour afficher la notification
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.sendNotification(notificationId, title, message);

        Log.d("NotificationReceiver", "Notification déclenchée : " + title);
    }
}