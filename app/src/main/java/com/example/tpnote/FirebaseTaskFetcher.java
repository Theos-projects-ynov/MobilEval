package com.example.tpnote;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseTaskFetcher {

    private final DatabaseReference tasksRef;
    private TaskDataListener taskDataListener;

    public FirebaseTaskFetcher() {
        // Référence sur le nœud "tasks"
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");
    }

    /**
     * Récupère toutes les tâches en fonction du userId
     */
    public void fetchTasksByUserID(String userId) {
        // Attention : "orderByChild("userId")" (comme dans ton JSON)
        tasksRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                                // Récupère les champs avec le bon type
                                Integer taskId       = taskSnapshot.child("taskId").getValue(Integer.class);
                                String title         = taskSnapshot.child("title").getValue(String.class);
                                String desc          = taskSnapshot.child("description").getValue(String.class);
                                String dateTime      = taskSnapshot.child("dateTime").getValue(String.class);
                                Boolean completed    = taskSnapshot.child("completed").getValue(Boolean.class);
                                String userIdFromDB  = taskSnapshot.child("userId").getValue(String.class);

                                // Evite les NullPointer. Si "completed" est null, on met false par défaut
                                if (completed == null) completed = false;
                                if (taskId == null) taskId = 0; // ou -1, au choix

                                if (taskDataListener != null) {
                                    taskDataListener.onTaskDataFetched(
                                            taskId,
                                            title != null ? title : "",
                                            dateTime != null ? dateTime : "",
                                            desc != null ? desc : "",
                                            completed,
                                            userIdFromDB != null ? userIdFromDB : ""
                                    );
                                }

                                // En cas d'échec
//                                if (taskDataListener != null) {
//                                    taskDataListener.onTaskDataFetchFailed("Erreur de récupération");
//                                }
                            }
                        } else {
                            // Aucune tâche trouvée
                            if (taskDataListener != null) {
                                taskDataListener.onTaskDataFetchFailed(
                                        "Aucune tâche pour l'userId : " + userId
                                );
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (taskDataListener != null) {
                            taskDataListener.onTaskDataFetchFailed(databaseError.getMessage());
                        }
                    }
                });
    }

    public void setTaskDataListener(TaskDataListener listener) {
        this.taskDataListener = listener;
    }

    public interface TaskDataListener {
        void onTaskDataFetched(
                int taskId,
                String title,
                String dateTime,
                String description,
                boolean completed,
                String userId
        );
        void onTaskDataFetchFailed(String errorMessage);
    }
}
