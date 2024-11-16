package com.example.tpnote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ALARM_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        String titre = intent.getStringExtra("titre");
        String description = intent.getStringExtra("description");

        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(titre)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent notificationIntent = new Intent(context, Activity1.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal d'Alarme";
            String description = "Notifications des alarmes";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
