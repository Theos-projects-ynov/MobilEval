package com.example.tpnote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmManagerHelper {

    public static void planifierNotification(Context context, String title, String message, int notificationId, Calendar dateHeure) {
        long triggerTimeMillis = dateHeure.getTimeInMillis();

        long currentTimeMillis = System.currentTimeMillis();
        if (triggerTimeMillis < currentTimeMillis) {
            triggerTimeMillis += 24 * 60 * 60 * 1000;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
            );
        }
    }
}
