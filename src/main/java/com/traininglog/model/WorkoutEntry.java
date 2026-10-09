package com.traininglog.model;

import java.sql.Date;

public class WorkoutEntry {
    private int id;
    private String level;
    private int calories;
    private Date logDate;

    public WorkoutEntry(int id, String level, int calories, Date logDate) {
        this.id = id;
        this.level = level;
        this.calories = calories;
        this.logDate = logDate;
    }

    public int getId() { return id; }
    public String getLevel() { return level; }
    public int getCalories() { return calories; }
    public Date getLogDate() { return logDate; }
}
