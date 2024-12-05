package com.example.tpnote;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Calendar;

public class Activity1 extends AppCompatActivity {

    private LinearLayout linearLayoutList;
    private int compteur = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        linearLayoutList = findViewById(R.id.linear_layout_list);

        Button boutonAjouter = findViewById(R.id.mon_bouton);

        boutonAjouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                afficherDialogueAjout();
            }
        });
    }

    private void afficherDialogueAjout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une Ligne");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);
        final TextView textViewSelectedTime = dialogView.findViewById(R.id.text_view_selected_time);
        Button buttonSelectTime = dialogView.findViewById(R.id.button_select_time);
        Button buttonCreate = dialogView.findViewById(R.id.button_create);

        final String[] heureSelectionnee = {""};

        buttonSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int heure = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(Activity1.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                String heureFormatee = String.format("%02d:%02d", selectedHour, selectedMinute);
                                textViewSelectedTime.setText(heureFormatee);
                                heureSelectionnee[0] = heureFormatee;
                            }
                        }, heure, minute, true);
                timePickerDialog.show();
            }
        });

        final AlertDialog dialog = builder.create();

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titre = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                String heure = heureSelectionnee[0];

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

                ajouterNouvelleLigne(titre, description, heure);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

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
        textViewHeure.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        nouvelleLigne.addView(verticalContainer);
        nouvelleLigne.addView(textViewHeure);

        linearLayoutList.addView(nouvelleLigne);

        String[] parts = heure.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        setAlarm(calendar);
//        scheduleAlarm(this, heure, titre, description);

        compteur++;
    }

    private void createNotificationChannel() {
        CharSequence name = "AlarmChannel";
        String description = "Canal pour les alarmes";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("alarmChannel", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setAlarm(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Alarme réglée pour " + calendar.getTime(), Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleAlarm(Context context, String heure, String titre, String description) {
        String[] parts = heure.split(":");
        if (parts.length != 2) {
            Toast.makeText(context, "Format de l'heure invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("titre", titre);
        intent.putExtra("description", description);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

            Toast.makeText(context, "Alarme planifiée à " + heure, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Erreur: AlarmManager non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
