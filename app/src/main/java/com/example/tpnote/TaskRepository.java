package com.example.tpnote;

import android.util.Log;
import com.example.tpnote.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final DatabaseReference mDatabase;

    public interface OnTaskCreatedListener {
        void onTaskCreated(boolean success, String message, int taskId);
    }

    public interface OnTasksFetchedListener {
        void onTasksFetched(List<Task> tasks);
    }

    public interface OnTaskFetchedListener {
        void onTaskFetched(Task task);
    }

    public TaskRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Crée une nouvelle tâche en incrémentant le compteur "taskCounter".
     */

    public void createTask(final String userId,
                           final String title,
                           final String description,
                           final String dateTime,
                           final boolean isCompleted,
                           final OnTaskCreatedListener listener) {

        final DatabaseReference counterRef = mDatabase.child("taskCounter");
        counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int currentCount = 0;
                if (snapshot.exists()) {
                    currentCount = snapshot.getValue(Integer.class);
                }

                int newTaskId = currentCount + 1;
                counterRef.setValue(newTaskId).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Task newTask = new Task(newTaskId, userId, title, description, dateTime, isCompleted);
                        mDatabase.child("tasks")
                                .child(String.valueOf(newTaskId))
                                .setValue(newTask)
                                .addOnCompleteListener(taskInner -> {
                                    if (taskInner.isSuccessful()) {
                                        if (listener != null) {
                                            listener.onTaskCreated(true, "Tâche créée avec succès", newTaskId);
                                        }
                                    } else {
                                        if (listener != null) {
                                            listener.onTaskCreated(false, "Erreur lors de la création de la tâche", -1);
                                        }
                                    }
                                });
                    } else {
                        if (listener != null) {
                            listener.onTaskCreated(false, "Erreur lors de la mise à jour du compteur", -1);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (listener != null) {
                    listener.onTaskCreated(false, "Lecture du compteur annulée : " + error.getMessage(), -1);
                }
            }
        });
    }


    /**
     * Exemple : récupère toutes les tâches.
     */
    public void getAllTasks(final OnTasksFetchedListener listener) {
        mDatabase.child("tasks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Task> tasks = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task t = child.getValue(Task.class);
                    if (t != null) {
                        tasks.add(t);
                    }
                }
                if (listener != null) {
                    listener.onTasksFetched(tasks);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (listener != null) {
                    listener.onTasksFetched(new ArrayList<>());
                }
            }
        });
    }
    public void deleteTask(int taskId, OnTaskDeletedListener listener) {
        DatabaseReference taskRef = mDatabase.child("tasks").child(String.valueOf(taskId));

        taskRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (listener != null) {
                    listener.onTaskDeleted(true, "Tâche supprimée avec succès");
                }
            } else {
                if (listener != null) {
                    listener.onTaskDeleted(false, "Erreur lors de la suppression de la tâche");
                }
            }
        });
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(boolean success, String message);
    }

}
