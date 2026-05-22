package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Date;

public class Attendance extends ActiveRecord {

    private Integer attendanceId;
    private Integer userId;
    private LocalDate workDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String notes;

    public Integer getAttendanceId() { return attendanceId; }
    public Integer getUserId() { return userId; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public String getNotes() { return notes; }

    public void setUserId(Integer userId) { this.userId = userId; executeUpdate("user_id", userId); }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }
    public void setNotes(String notes) { this.notes = notes; }

    public Attendance(Integer userId) {
        this.userId = userId;
        this.workDate = LocalDate.now();
        this.clockIn = LocalDateTime.now();
    }

    public Attendance(Map<String, Object> map) { fromMap(map); }

    // ActiveRecord Implementation
    @Override protected String tableName() { return "attendance"; }
    @Override protected String primaryKeyColumn() { return "attendance_id"; }
    @Override protected Integer getId() { return getAttendanceId(); }
    @Override protected void setId(Integer value) { this.attendanceId = value; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user_id", userId);
        map.put("work_date", workDate);
        map.put("clock_in", clockIn);
        map.put("clock_out", clockOut);
        map.put("notes", notes);
        return map;
    }
    @Override
    public void fromMap(Map<String, Object> map) {
        this.attendanceId = (Integer) map.get("attendance_id");
        this.userId = (Integer) map.get("user_id");
        this.notes = (String)  map.get("notes");

        this.workDate = (map.get("work_date") instanceof java.sql.Date) ?
                ((java.sql.Date) map.get("work_date")).toLocalDate() : (LocalDate) map.get("work_date");

        this.clockIn = (LocalDateTime) map.get("clock_in");
        this.clockOut = (LocalDateTime) map.get("clock_out");
    }

    // Business
    public void doClockOut() {
        this.clockOut = LocalDateTime.now();
        executeUpdate("clock_out", clockOut);
    }

    public long getWorkDurationMinutes() {
        if (getClockIn() != null && getClockOut() != null) {
            return Duration.between(getClockIn(), getClockOut()).toMinutes();
        }
        return 0;
    }
}
