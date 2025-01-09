package com.example.tpnote;

import android.app.DatePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tpnote.model.Task;

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

            Toast.makeText(this, titre + ": " + message, Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        linearLayoutList = findViewById(R.id.linear_layout_list);
        Button boutonAjouter = findViewById(R.id.mon_bouton);

        // Identifiant utilisateur fictif
        String userId = "06 12 34 56 79";

        // Charger les tâches existantes pour l'utilisateur
        chargerTachesExistantes(userId);

        // Gestion du clic pour ajouter une tâche
        boutonAjouter.setOnClickListener(view -> afficherDialogueAjout(userId));
    }

    private void chargerTachesExistantes(String userId) {
        taskFetcher.setTaskDataListener(new FirebaseTaskFetcher.TaskDataListener() {
            @Override
            public void onTaskDataFetched(int taskId, String title, String dateTime, String description, boolean completed, String userIdFromDB) {
                Log.d(TAG, "Tâche récupérée : ID = " + taskId + ", Titre = " + title + ", Date = " + dateTime + ", Description = " + description);

                // Ajout de la tâche dans l'interface
                String heureExtraite = extraireHeureDepuisDate(dateTime);
                ajouterNouvelleLigne(title, description, heureExtraite);
            }

            @Override
            public void onTaskDataFetchFailed(String errorMessage) {
                Toast.makeText(Activity1.this, "Erreur : " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        taskFetcher.fetchTasksByUserID(userId);
    }

    private void afficherDialogueAjout(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une Tâche");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);
        final TextView textViewSelectedDateTime = dialogView.findViewById(R.id.text_view_selected_time);
        Button buttonSelectDateTime = dialogView.findViewById(R.id.button_select_time);
        Button buttonCreate = dialogView.findViewById(R.id.button_create);

        final Calendar calendar = Calendar.getInstance(); // Stocke la date/heure sélectionnée

        buttonSelectDateTime.setOnClickListener(view -> {
            final Calendar now = Calendar.getInstance();
            int annee = now.get(Calendar.YEAR);
            int mois = now.get(Calendar.MONTH);
            int jour = now.get(Calendar.DAY_OF_MONTH);

            // Afficher le DatePickerDialog
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                int heure = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);

                // Afficher le TimePickerDialog
                new TimePickerDialog(this, (timePicker, hourOfDay, minute1) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);
                    calendar.set(Calendar.SECOND, 0);

                    // Met à jour l'affichage de la date/heure
                    String dateTimeFormatee = String.format("%02d/%02d/%d %02d:%02d",
                            dayOfMonth, month + 1, year, hourOfDay, minute1);
                    textViewSelectedDateTime.setText(dateTimeFormatee);
                    Log.d(TAG, "Date/Heure sélectionnée : " + dateTimeFormatee);

                }, heure, minute, true).show();

            }, annee, mois, jour).show();
        });

        final AlertDialog dialog = builder.create();

        buttonCreate.setOnClickListener(view -> {
            String titre = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String dateTime = textViewSelectedDateTime.getText().toString().trim();

            if (titre.isEmpty()) {
                editTextTitle.setError("Le titre est requis");
                return;
            }

            if (description.isEmpty()) {
                editTextDescription.setError("La description est requise");
                return;
            }

            if (dateTime.isEmpty()) {
                textViewSelectedDateTime.setError("La date et l'heure sont requises");
                return;
            }

            // Ajoute la tâche dans l'interface
            ajouterNouvelleLigne(titre, description, dateTime);

            // Enregistre la tâche dans Firebase

            repository.createTask(
                    userId,
                    titre,
                    description,
                    dateTime,
                    false,
                    (success, message, taskId) -> {
                        if (success) {
                            Log.d(TAG, "Tâche créée dans Firebase : " + message);

                            // Planifie la notification
                            Calendar calendar2 = Calendar.getInstance();
                            // Extrait les informations de date et d'heure
                            String[] dateTimeParts = dateTime.split(" ");
                            if (dateTimeParts.length == 2) {
                                String[] dateParts = dateTimeParts[0].split("/");
                                String[] timeParts = dateTimeParts[1].split(":");

                                if (dateParts.length == 3 && timeParts.length == 2) {
                                    int year = Integer.parseInt(dateParts[2]);
                                    int month = Integer.parseInt(dateParts[1]) - 1; // Les mois commencent à 0
                                    int day = Integer.parseInt(dateParts[0]);
                                    int hour = Integer.parseInt(timeParts[0]);
                                    int minute = Integer.parseInt(timeParts[1]);

                                    calendar2.set(Calendar.YEAR, year);
                                    calendar2.set(Calendar.MONTH, month);
                                    calendar2.set(Calendar.DAY_OF_MONTH, day);
                                    calendar2.set(Calendar.HOUR_OF_DAY, hour);
                                    calendar2.set(Calendar.MINUTE, minute);
                                    calendar2.set(Calendar.SECOND, 0);

                                    // Planifie la notification si la date/heure est valide
                                    if (calendar2.getTimeInMillis() > System.currentTimeMillis()) {
                                        AlarmManagerHelper.planifierNotification(
                                                Activity1.this,
                                                titre,
                                                description,
                                                generateUniqueNotificationId(),
                                                calendar2
                                        );
                                    } else {
                                        Log.e(TAG, "La date/heure sélectionnée est dans le passé.");
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "Erreur lors de la création de la tâche : " + message);
                        }
                    }
            );

            dialog.dismiss();
        });


        dialog.show();
    }

    private void planifierNotification(Context context, String title, String message, int notificationId, Calendar dateHeure) {
        long triggerTimeMillis = dateHeure.getTimeInMillis();

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
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
            );
        }
        Log.d(TAG, "Notification planifiée pour : " + dateHeure.getTime());
    }

    private int generateUniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void ajouterNouvelleLigne(String titre, String description, String heure) {
        Log.d(TAG, "Ajout d'une ligne : Titre = " + titre + ", Description = " + description + ", Heure = " + heure);

        LinearLayout nouvelleLigne = new LinearLayout(this);
        nouvelleLigne.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10); // Marges autour de la ligne
        nouvelleLigne.setLayoutParams(params);

        LinearLayout verticalContainer = new LinearLayout(this);
        verticalContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(
                0, // Largeur 0dp pour utiliser layout_weight
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f // Utilisation de layout_weight pour occuper l'espace restant
        );
        verticalContainer.setLayoutParams(verticalParams);
        verticalContainer.setPadding(8, 8, 8, 8);

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

        linearLayoutList.invalidate(); // Forcer l'affichage
        Log.d(TAG, "Nouvelle ligne ajoutée avec succès !");
    }

    private String extraireHeureDepuisDate(String dateTime) {
        if (dateTime == null || !dateTime.contains("T")) {
            return "";
        }
        String[] parts = dateTime.split("T");
        if (parts.length >= 2) {
            String[] timeParts = parts[1].split(":");
            if (timeParts.length >= 2) {
                return timeParts[0] + ":" + timeParts[1];
            }
        }
        return "";
    }
}
