package com.example.tpnote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;

import android.Manifest;

public class Menu extends AppCompatActivity {

    private static final String TAG = Menu.class.getSimpleName();

    private NotificationHelper notificationHelper;
    Button button1, button2, button3, button4;
    TaskRepository repository = new TaskRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);



        afficherPopupAutorisationNotifs();
        notificationHelper = new NotificationHelper(this);

        notificationHelper.sendSimpleNotification(1, "TEST 2", "Message de test");

        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        setContentView(R.layout.menu);


        setContentView(R.layout.menu);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        Log.v(TAG, "Ceci est un log verbose");
        Log.d(TAG, "Ceci est un log de débogage");
        Log.i(TAG, "Ceci est un log informatif");


        Log.v(TAG, "Ceci est un log verbose 2");

        // Bouton 1
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, Activity1.class);
                startActivity(intent);
            }
        });

        // Bouton 2
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, Activity2.class);
                startActivity(intent);
            }
        });

        // Bouton 3
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, Activity3.class);
                startActivity(intent);
            }
        });

        // Bouton 4
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, Settings.class);
                startActivity(intent);
            }
        });
    }

    private void afficherPopupAutorisationNotifs() {
        // Vérifiez si l'utilisateur a déjà donné son consentement
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean autorisationDonnee = prefs.getBoolean("notificationsAutorisees", false);

        if (!autorisationDonnee) {
            // Créez et affichez la boîte de dialogue
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Autorisation des Notifications");
            builder.setMessage("L'application souhaite vous envoyer des notifications pour vous rappeler vos tâches. Acceptez-vous ?");

            builder.setPositiveButton("Oui", (dialog, which) -> {
                // Sauvegarder le consentement
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("notificationsAutorisees", true);
                editor.apply();

                Toast.makeText(this, "Notifications activées", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Non", (dialog, which) -> {
                Toast.makeText(this, "Notifications désactivées", Toast.LENGTH_SHORT).show();
            });

            builder.create().show();
        }
    }

    public void sendSimpleNotification(int notificationId, String titre, String message) {
        final String CHANNEL_ID = "task_reminder_channel";
        final String CHANNEL_NAME = "Rappel de Tâches";
        final String CHANNEL_DESC = "Notifications pour vous rappeler vos tâches planifiées";
        Context mContext = this;

        SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        boolean autorisationDonnee = prefs.getBoolean("notificationsAutorisees", false);

        if (autorisationDonnee) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(titre)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            notificationManager.notify(notificationId, builder.build());
        }
    }

}