package com.example.tpnote;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("notificationId", 0);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.sendSimpleNotification(notificationId, title, message);
    }

    private Notification getNotification(Context context) {
        Intent notificationIntent = new Intent(context, Activity1.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("CHANNEL_ID", "Alarm Time....", NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setContentTitle("Title")
                .setContentText("Your Message")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

}
