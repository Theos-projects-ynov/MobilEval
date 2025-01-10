package com.example.tpnote;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;

public class Menu extends AppCompatActivity {

    private static final String TAG = Menu.class.getSimpleName();
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_USER_ID = "userId";
    private static final int EXIT_DELAY = 2000; // Délai avant réinitialisation (en millisecondes)
    private boolean backPressedOnce = false;

    private NotificationHelper notificationHelper;
    private Button button1, button2, button3, button4;
    private TaskRepository repository = new TaskRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);

        afficherPopupAutorisationNotifs();
        notificationHelper = new NotificationHelper(this);

        super.onCreate(savedInstanceState);

        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);

        FirebaseApp.initializeApp(this);
        setContentView(R.layout.menu);

        notificationHelper = new NotificationHelper(this);
        notificationHelper.sendSimpleNotification(1, "TEST 2", "Message de test");

        verifierNumeroUtilisateur();
        afficherPopupAutorisationNotifs();

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        Log.v(TAG, "Ceci est un log verbose");
        Log.d(TAG, "Ceci est un log de débogage");
        Log.i(TAG, "Ceci est un log informatif");

        button1.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Activity1.class);
            startActivity(intent);
        });

        button2.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Activity2.class);
            startActivity(intent);
        });

        button3.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Activity3.class);
            startActivity(intent);
        });

        button4.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Settings.class);
            startActivity(intent);
        });
    }

    private void verifierNumeroUtilisateur() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString(PREF_USER_ID, null);

        if (userId == null) {
            afficherPopupSaisieNumero();
        }
    }

    private void afficherPopupSaisieNumero() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Numéro de téléphone requis");

        final EditText input = new EditText(this);
        input.setHint("Entrez votre numéro de téléphone");
        builder.setView(input);

        builder.setPositiveButton("Valider", (dialog, which) -> {
            String numero = input.getText().toString().trim();
            if (!numero.isEmpty() && numero.matches("\\d{10}")) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PREF_USER_ID, numero);
                editor.apply();

                Toast.makeText(this, "Numéro enregistré : " + numero, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Veuillez entrer un numéro valide", Toast.LENGTH_SHORT).show();
                afficherPopupSaisieNumero();
            }
        });

        builder.setCancelable(false);
        builder.create().show();
    }

    private void afficherPopupAutorisationNotifs() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean autorisationDonnee = prefs.getBoolean("notificationsAutorisees", false);

        if (!autorisationDonnee) {
            // Créez et affichez la boîte de dialogue
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Autorisation des Notifications");
            builder.setMessage("L'application souhaite vous envoyer des notifications pour vous rappeler vos tâches. Acceptez-vous ?");

            builder.setPositiveButton("Oui", (dialog, which) -> {
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

    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.backPressedOnce = true;
        Toast.makeText(this, "Appuyez de nouveau pour quitter", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> backPressedOnce = false, EXIT_DELAY);
    }
}
