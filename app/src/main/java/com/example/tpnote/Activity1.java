package com.example.tpnote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tpnote.model.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Activity1 extends AppCompatActivity {

    private LinearLayout linearLayoutList;
    private TaskRepository repository = new TaskRepository();
    private FirebaseTaskFetcher taskFetcher = new FirebaseTaskFetcher();

    private static final String TAG = "Activity1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1);

        // Vérifie si une notification a ouvert l'activité
        if (getIntent().hasExtra("title") && getIntent().hasExtra("message")) {
            String titre = getIntent().getStringExtra("title");
            String message = getIntent().getStringExtra("message");

            // Affiche un toast ou ouvre un écran spécifique
            Toast.makeText(this, titre + ": " + message, Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        linearLayoutList = findViewById(R.id.linear_layout_list);
        Button boutonAjouter = findViewById(R.id.mon_bouton);

        // On récupère un userId (fictif) :
        String userId = "06 12 34 56 79";

        // On charge les tâches existantes pour ce userId
        chargerTachesExistantes(userId);

        // On gère le clic pour ajouter une nouvelle tâche
        boutonAjouter.setOnClickListener(view -> {
            afficherDialogueAjout(userId);
        });

    }

    /**
     * Récupère toutes les tâches pour le userId
     */
    private void chargerTachesExistantes(String userId) {
        // On définit le listener
        taskFetcher.setTaskDataListener(new FirebaseTaskFetcher.TaskDataListener() {
            @Override
            public void onTaskDataFetched(int taskId,
                                          String title,
                                          String dateTime,
                                          String description,
                                          boolean completed,
                                          String userIdFromDB) {
                // Convertir "dateTime" => "HH:MM" si tu veux juste afficher l'heure
                String heureExtraite = extraireHeureDepuisDate(dateTime);

                // On ajoute visuellement la tâche
                ajouterNouvelleLigne(title, description, heureExtraite);
            }

            @Override
            public void onTaskDataFetchFailed(String errorMessage) {
                Toast.makeText(Activity1.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // On lance la requête
        taskFetcher.fetchTasksByUserID(userId);
    }

    /**
     * Ouvre la boîte de dialogue pour créer une tâche
     */
    private void afficherDialogueAjout(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une Tâche");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        final EditText editTextTitle        = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription  = dialogView.findViewById(R.id.edit_text_description);
        final TextView textViewSelectedTime = dialogView.findViewById(R.id.text_view_selected_time);
        Button buttonSelectTime             = dialogView.findViewById(R.id.button_select_time);
        Button buttonCreate                 = dialogView.findViewById(R.id.button_create);

        final String[] heureSelectionnee = {""};

        buttonSelectTime.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int heure  = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Activity1.this,
                    (timePicker, selectedHour, selectedMinute) -> {
                        String heureFormatee = String.format("%02d:%02d", selectedHour, selectedMinute);
                        textViewSelectedTime.setText(heureFormatee);
                        heureSelectionnee[0] = heureFormatee;
                    },
                    heure, minute,
                    true
            );
            timePickerDialog.show();
        });

        final AlertDialog dialog = builder.create();

        buttonCreate.setOnClickListener(view -> {
            String titre       = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String heure       = heureSelectionnee[0];

            if (titre.isEmpty()) {
                editTextTitle.setError("Le titre est requis");
                return;
            }
            if (description.isEmpty()) {
                editTextDescription.setError("La description est requise");
                return;
            }
            if (heure.isEmpty()) {
                textViewSelectedTime.setError("L'heure est requise");
                return;
            }

            // 1) On l’ajoute visuellement
            ajouterNouvelleLigne(titre, description, heure);

            // 2) On enregistre dans Firebase via le TaskRepository
            String dateComplet = "2025-12-05T" + heure + ":00"; // ex. format "YYYY-MM-DDTHH:MM:SS"
            repository.createTask(
                    userId,
                    titre,
                    description,
                    dateComplet,
                    false,
                    (success, message, taskId) -> {
                        if (success) {
                            Log.v(TAG, "SUCCESS FIREBASE: " + message);

                            // Découper l'heure
                            String[] parties = heure.split(":");
                            int heuresInt = Integer.parseInt(parties[0]);
                            int minutesInt = Integer.parseInt(parties[1]);

                            // Planification de la notification
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, heuresInt);
                            calendar.set(Calendar.MINUTE, minutesInt);
                            calendar.set(Calendar.SECOND, 0);

                            int notificationId = taskId > 0 ? taskId : generateUniqueNotificationId();
                            planifierNotification(this, titre, "Rappel de tâche : " + description, notificationId, calendar);
                        } else {
                            Log.v(TAG, "ERROR FIREBASE: " + message);
                        }
                    }
            );

            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Génère un ID unique basé sur le timestamp
     */
    private int generateUniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    /**
     * Ajoute une nouvelle ligne (à l'écran) dans le LinearLayout
     */
    private void ajouterNouvelleLigne(String titre, String description, String heure) {
        LinearLayout nouvelleLigne = new LinearLayout(this);
        nouvelleLigne.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);
        nouvelleLigne.setLayoutParams(params);

        LinearLayout verticalContainer = new LinearLayout(this);
        verticalContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        verticalContainer.setLayoutParams(verticalParams);

        TextView textViewTitre = new TextView(this);
        textViewTitre.setText(titre);
        textViewTitre.setTextSize(18f);
        textViewTitre.setTypeface(null, android.graphics.Typeface.BOLD);
        textViewTitre.setPadding(0, 0, 0, 4);

        TextView textViewDescription = new TextView(this);
        textViewDescription.setText(description);
        textViewDescription.setTextSize(16f);
        textViewDescription.setPadding(0, 0, 0, 4);

        verticalContainer.addView(textViewTitre);
        verticalContainer.addView(textViewDescription);

        TextView textViewHeure = new TextView(this);
        textViewHeure.setText(heure);
        textViewHeure.setTextSize(16f);
        textViewHeure.setTypeface(null, android.graphics.Typeface.ITALIC);
        textViewHeure.setPadding(16, 0, 0, 0);

        nouvelleLigne.addView(verticalContainer);
        nouvelleLigne.addView(textViewHeure);
        linearLayoutList.addView(nouvelleLigne);
    }

    /**
     * Exemple : extraire HH:MM d'une chaîne "YYYY-MM-DDTHH:MM:SS".
     */
    private String extraireHeureDepuisDate(String dateTime) {
        if (dateTime == null || !dateTime.contains("T")) {
            return "";
        }
        String[] parts = dateTime.split("T");
        if (parts.length >= 2) {
            // ex. "14:30:00"
            String[] timeParts = parts[1].split(":");
            if (timeParts.length >= 2) {
                return timeParts[0] + ":" + timeParts[1];
            }
        }
        return "";
    }

    private void planifierNotification(Context context, String title, String message, int notificationId, Calendar dateHeure) {
        long triggerTimeMillis = dateHeure.getTimeInMillis();

        // Vérifie que l'heure n'est pas passée
        long currentTimeMillis = System.currentTimeMillis();
        if (triggerTimeMillis < currentTimeMillis) {
            triggerTimeMillis += 24 * 60 * 60 * 1000; // Ajoute un jour si l'heure est déjà passée
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


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
