package com.example.tpnote;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.graphics.Color;
import android.util.AttributeSet;

public class Reminders extends AppCompatActivity {
    private LinearLayout linearLayoutList;
    private TaskManager repository;
    private FirebaseTaskFetcher taskFetcher = new FirebaseTaskFetcher();

    private static final String TAG = "Activity1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1);
        initApp();
    }

    private void initApp() {
        repository = new TaskManager(this);
        if (repository == null) {
            Log.e(TAG, "Erreur lors de l'initialisation de TaskManager");
            return;
        }
        linearLayoutList = findViewById(R.id.linear_layout_list);
        Button boutonAjouter = findViewById(R.id.mon_bouton);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        logUserId(userId);
        chargerTachesExistantes(userId);

        boutonAjouter.setOnClickListener(view -> afficherDialogueAjout(userId));
    }

    private void logUserId(String userId) {
        Log.d("PREFS_TEST", "UserId actuel : " + (userId != null ? userId : "Aucun ID trouvé"));
    }

    private void chargerTachesExistantes(String userId) {
        if (taskFetcher == null) {
            Log.e(TAG, "Erreur : taskFetcher n'est pas initialisé.");
            return;
        }
        taskFetcher.setTaskDataListener(new FirebaseTaskFetcher.TaskDataListener() {
            @Override
            public void onTaskDataFetched(int taskId, String title, String dateTime, String description, boolean completed, String userIdFromDB) {
                if (title == null || title.isEmpty()) title = "Sans titre";
                if (description == null || description.isEmpty()) description = "Pas de description";
                if (dateTime == null || dateTime.isEmpty()) dateTime = "Date/Heure inconnue";
                ajouterNouvelleLigne(title, description, dateTime, taskId);
            }

            @Override
            public void onTaskDataFetchFailed(String errorMessage) {
                Toast.makeText(Reminders.this, "Erreur : " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        taskFetcher.fetchTasksByUserID(userId);
    }

    private void afficherDialogueAjout(String userId) {
        AlertDialog dialog = createAddTaskDialog(userId);
        dialog.show();
    }

    private AlertDialog createAddTaskDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une Tâche");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);
        final TextView textViewSelectedDateTime = dialogView.findViewById(R.id.text_view_selected_time);
        Button buttonSelectDateTime = dialogView.findViewById(R.id.button_select_time);
        Button buttonCreate = dialogView.findViewById(R.id.button_create);

        final Calendar calendar = Calendar.getInstance();
        setupDateTimeSelector(buttonSelectDateTime, textViewSelectedDateTime, calendar);

        final AlertDialog dialog = builder.create();
        setupCreateTaskButton(buttonCreate, dialog, userId, editTextTitle, editTextDescription, textViewSelectedDateTime, calendar);

        return dialog;
    }

    private void setupDateTimeSelector(Button button, TextView textView, Calendar calendar) {
        button.setOnClickListener(view -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (datePicker, year, month, day) -> {
                calendar.set(year, month, day);
                selectTime(textView, calendar);
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void selectTime(TextView textView, Calendar calendar) {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (timePicker, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            textView.setText(formatDateTime(calendar));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private String formatDateTime(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void setupCreateTaskButton(Button button, AlertDialog dialog, String userId, EditText title, EditText description, TextView dateTimeView, Calendar calendar) {
        button.setOnClickListener(view -> {
            if (validateInputs(title, description, dateTimeView)) {
                createTaskAndDismiss(dialog, userId, title.getText().toString(), description.getText().toString(), dateTimeView.getText().toString(), calendar);
            }
        });
    }

    private boolean validateInputs(EditText title, EditText description, TextView dateTimeView) {
        if (title.getText().toString().trim().isEmpty()) {
            title.setError("Le titre est requis");
            return false;
        }
        if (description.getText().toString().trim().isEmpty()) {
            description.setError("La description est requise");
            return false;
        }
        if (dateTimeView.getText().toString().trim().isEmpty()) {
            dateTimeView.setError("La date et l'heure sont requises");
            return false;
        }
        return true;
    }

    private void createTaskAndDismiss(AlertDialog dialog, String userId, String title, String description, String dateTime, Calendar calendar) {
        int taskId = generateUniqueNotificationId();
        ajouterNouvelleLigne(title, description, dateTime, taskId);
        repository.createTask(userId, title, description, dateTime, false, (success, message, id) -> {
            if (success) {
                planifierNotificationAvecLogs(title, description, calendar);
            } else {
                Log.e(TAG, "Erreur lors de la création : " + message);
            }
        });
        dialog.dismiss();
    }

    private int generateUniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void ajouterNouvelleLigne(String titre, String description, String dateTime, int taskId) {
        LinearLayout nouvelleLigne = createTaskLayout(titre, description, dateTime, taskId);

        if (linearLayoutList == null) {
            Log.e(TAG, "Erreur : linearLayoutList n'est pas initialisé.");
            return;
        }

        linearLayoutList.addView(nouvelleLigne);
        linearLayoutList.requestLayout();
    }

    private LinearLayout createTaskLayout(String titre, String description, String dateTime,int taskId) {
        LinearLayout taskLayout = new LinearLayout(this);
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setLayoutParams(createLayoutParams());

        LinearLayout verticalContainer = createVerticalContainer(titre, description);
        TextView textViewDateTime = createDateTimeTextView(dateTime);

        taskLayout.addView(verticalContainer);
        taskLayout.addView(textViewDateTime);

        // Ajouter un bouton "X" pour s'assurer qu'il est toujours présent
        CustomDeleteButton deleteButton = new CustomDeleteButton(this);
        deleteButton.setTag("delete_button"); // Ajouter un tag pour éviter les doublons
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        taskLayout.addView(deleteButton);

        deleteButton.setOnClickListener(view -> {
            Log.d("DEBUG", "Delete button clicked1");
            repository.deleteTask(taskId, (success, message) -> {
                if (success) {
                    Log.d("DEBUG", "Task deleted successfully");
                    linearLayoutList.removeView(taskLayout);
                    linearLayoutList.invalidate(); // Force a refresh of the layout
                    Toast.makeText(this, "Task deleted: " + titre, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erreur : " + message, Toast.LENGTH_SHORT).show();
                }
            });        });

        return taskLayout;
    }

    private LinearLayout.LayoutParams createLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 5, 0, 5);
        return params;
    }

    private LinearLayout createVerticalContainer(String titre, String description) {
        LinearLayout verticalContainer = new LinearLayout(this);
        verticalContainer.setOrientation(LinearLayout.VERTICAL);
        verticalContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        verticalContainer.setPadding(8, 8, 8, 8);

        verticalContainer.addView(createTextView(titre, 18f, true));
        verticalContainer.addView(createTextView(description, 16f, false));

        return verticalContainer;
    }

    private TextView createTextView(String text, float textSize, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setPadding(0, 0, 0, 4);
        if (bold) textView.setTypeface(null, android.graphics.Typeface.BOLD);
        return textView;
    }

    private TextView createDateTimeTextView(String dateTime) {
        TextView textView = new TextView(this);
        textView.setText(dateTime);
        textView.setTextSize(16f);
        textView.setTypeface(null, android.graphics.Typeface.ITALIC);
        textView.setPadding(16, 0, 0, 0);
        return textView;
    }

    public static class CustomDeleteButton extends View {
        private Paint paint;

        public CustomDeleteButton(Context context) {
            super(context);
            init();
        }

        public CustomDeleteButton(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CustomDeleteButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            int width = getWidth();
            int height = getHeight();

            if (width == 0 || height == 0) {
                setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            float padding = 10;
            float crossSize = Math.min(width, height) - padding;

            canvas.drawLine(padding, padding, width - padding, height - padding, paint);
            canvas.drawLine(width - padding, padding, padding, height - padding, paint);
        }
    }

    private void planifierNotificationAvecLogs(String titre, String description, Calendar dateHeure) {
        int notificationId = generateUniqueNotificationId();
        TaskManager.planifierNotification(this, titre, description, notificationId, dateHeure);

        Log.d("Reminders", "Notification planifiée : " + titre + " à " + dateHeure.getTime().toString());
    }
}
