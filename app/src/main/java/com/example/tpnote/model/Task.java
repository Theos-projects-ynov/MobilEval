package com.example.tpnote.model;

public class Task {
    private int taskId;          // correspond à "taskId": 1
    private String userId;       // correspond à "userId": "..."
    private String title;        // correspond à "title": "..."
    private String description;  // correspond à "description": "..."
    private String dateTime;     // correspond à "dateTime": "..."
    private boolean completed;   // correspond à "completed": false

    // Constructeur vide obligatoire pour Firebase
    public Task() {
    }

    public Task(int taskId, String userId, String title, String description, String dateTime, boolean completed) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.completed = completed;
    }

    public int getTaskId() {
        return taskId;
    }
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
