package com.example.tpnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper helper = new NotificationHelper(context);
        String titre = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("notificationId", 0);

        helper.sendSimpleNotification(notificationId, titre, message);
    }
}
