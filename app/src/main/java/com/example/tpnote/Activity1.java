package com.example.tpnote;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
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
        final TextView textViewSelectedDateTime = dialogView.findViewById(R.id.text_view_selected_time);
        Button buttonSelectDateTime = dialogView.findViewById(R.id.button_select_time);
        Button buttonCreate = dialogView.findViewById(R.id.button_create);

        final String[] dateTimeSelectionnee = {""};

        buttonSelectDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int annee = c.get(Calendar.YEAR);
                int mois = c.get(Calendar.MONTH);
                int jour = c.get(Calendar.DAY_OF_MONTH);
                int heure = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // Afficher le DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(Activity1.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                // Une fois la date sélectionnée, afficher l'horloge
                                TimePickerDialog timePickerDialog = new TimePickerDialog(Activity1.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                                // Combiner la date et l'heure
                                                String dateTimeFormatee = String.format("%02d/%02d/%d %02d:%02d",
                                                        selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                                                textViewSelectedDateTime.setText(dateTimeFormatee);
                                                dateTimeSelectionnee[0] = dateTimeFormatee;
                                            }
                                        }, heure, minute, true);
                                timePickerDialog.show();
                            }
                        }, annee, mois, jour);
                datePickerDialog.show();
            }
        });

        final AlertDialog dialog = builder.create();

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titre = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                String dateTime = dateTimeSelectionnee[0];

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

                ajouterNouvelleLigne(titre, description, dateTime);

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

        compteur++;
    }

}