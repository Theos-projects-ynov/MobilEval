package com.example.tpnote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Import extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imports);

        // Ajouter la logique du bouton "Importer"
        Button boutonImporter = findViewById(R.id.button);
        EditText editTextTelephone = findViewById(R.id.editTextText);

        boutonImporter.setOnClickListener(view -> {
            String nouveauUserId = editTextTelephone.getText().toString().trim();

            if (!nouveauUserId.isEmpty() && nouveauUserId.matches("\\d{10}")) {
                // Sauvegarder le nouveau userId dans les préférences
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userId", nouveauUserId);
                editor.apply();

                Toast.makeText(this, "userId mis à jour : " + nouveauUserId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Veuillez entrer un numéro de téléphone valide", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
