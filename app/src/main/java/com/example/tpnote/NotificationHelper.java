package com.example.tpnote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationHelper {

    private static final String CHANNEL_ID = "mon_channel_id";
    private static final String CHANNEL_NAME = "Mon Canal de Notifications";
    private static final String CHANNEL_DESC = "Canal pour les notifications de l’application";
    private Context mContext;

    public NotificationHelper(Context context) {
        this.mContext = context;

        // Création du canal de notification si nécessaire
        createNotificationChannel();
    }

    public void scheduleNotification(String title, String message, int notificationId, long triggerTimeMillis) {
        Intent intent = new Intent(mContext, Activity1.class); // Peut être remplacé par un BroadcastReceiver si nécessaire
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        channel.setDescription(CHANNEL_DESC);
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        channel.enableVibration(true);

        NotificationManager manager = mContext.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Envoie une notification simple avec un titre et un texte.
     * @param notificationId Identifiant unique pour la notification
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     */
    public void sendSimpleNotification(int notificationId, String titre, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titre)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Envoie une notification avec des actions supplémentaires
     * @param notificationId Identifiant unique pour la notification
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     * @param actionTitle Titre de l'action
     * @param actionIntent Intent pour l'action (par exemple un PendingIntent)
     */
    public void sendNotification(int notificationId, String titre, String message) {

        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titre)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(mContext).notify(notificationId, notification);
    }

}
