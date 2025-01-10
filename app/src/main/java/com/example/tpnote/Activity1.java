package com.example.tpnote;

import android.app.DatePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        Log.d("PREFS_TEST", "UserId actuel : " + (userId != null ? userId : "Aucun ID trouvé"));
        ;

        // Charger les tâches existantes pour l'utilisateur
        chargerTachesExistantes(userId);

        // Gestion du clic pour ajouter une tâche
        boutonAjouter.setOnClickListener(view -> afficherDialogueAjout(userId));
    }

    private void chargerTachesExistantes(String userId) {
        taskFetcher.setTaskDataListener(new FirebaseTaskFetcher.TaskDataListener() {
            @Override
            public void onTaskDataFetched(int taskId, String title, String dateTime, String description, boolean completed, String userIdFromDB) {
                ajouterNouvelleLigne(title, description, dateTime, taskId); // Ajoute taskId ici
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

            // Ajout visuel de la tâche
            int generatedTaskId = generateUniqueNotificationId();
            ajouterNouvelleLigne(titre, description, dateTime, generatedTaskId);

            // Enregistrement Firebase
            repository.createTask(
                    userId,
                    titre,
                    description,
                    dateTime,
                    false,
                    (success, message, taskId) -> {
                        if (success) {
                            Log.d(TAG, "Tâche créée dans Firebase : " + message);

                            // Planification de la notification
                            Calendar calendar2 = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            try {
                                calendar2.setTime(sdf.parse(dateTime));
                                AlarmManagerHelper.planifierNotification(
                                        Activity1.this,
                                        titre,
                                        description,
                                        taskId,
                                        calendar2
                                );
                            } catch (ParseException e) {
                                Log.e(TAG, "Erreur lors de la conversion de la date/heure : " + e.getMessage());
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

    private int generateUniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void ajouterNouvelleLigne(String titre, String description, String dateTime, int taskId) {
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

        TextView textViewDateTime = new TextView(this);
        textViewDateTime.setText(dateTime);
        textViewDateTime.setTextSize(16f);
        textViewDateTime.setTypeface(null, android.graphics.Typeface.ITALIC);
        textViewDateTime.setPadding(16, 0, 0, 0);

        // Bouton pour supprimer la tâche
        Button buttonDelete = new Button(this);
        buttonDelete.setText("✖");
        buttonDelete.setTextSize(18f);
        buttonDelete.setPadding(8, 8, 8, 8);

        buttonDelete.setOnClickListener(view -> {
            // Supprimer la tâche dans Firebase
            repository.deleteTask(taskId, (success, message) -> {
                if (success) {
                    linearLayoutList.removeView(nouvelleLigne);
                    Toast.makeText(this, "Tâche supprimée : " + titre, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erreur : " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        nouvelleLigne.addView(verticalContainer);
        nouvelleLigne.addView(textViewDateTime);
        nouvelleLigne.addView(buttonDelete);
        linearLayoutList.addView(nouvelleLigne);
    }
}
