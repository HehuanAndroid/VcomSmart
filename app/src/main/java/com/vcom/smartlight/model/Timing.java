package com.vcom.smartlight.model;

/**
 * @author Banlap on 2021/6/1
 */
public class Timing {
    private String timingId;
    private String month;
    private String day;
    private String week;
    private String hour;
    private String minute;
    private String sceneMeshId;
    private boolean isEnabled;

    private String sceneId;
    private String timingTime;
    private String timingWeek;
    private String timingIsStart;
    private String timingCreateTime;
    private String timingUpdateTime;


    public String getTimingId() {
        return timingId;
    }

    public void setTimingId(String timingId) {
        this.timingId = timingId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getSceneMeshId() {
        return sceneMeshId;
    }

    public void setSceneMeshId(String sceneMeshId) {
        this.sceneMeshId = sceneMeshId;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }


    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getTimingTime() {
        return timingTime;
    }

    public void setTimingTime(String timingTime) {
        this.timingTime = timingTime;
    }

    public String getTimingWeek() {
        return timingWeek;
    }

    public void setTimingWeek(String timingWeek) {
        this.timingWeek = timingWeek;
    }

    public String getTimingIsStart() {
        return timingIsStart;
    }

    public void setTimingIsStart(String timingIsStart) {
        this.timingIsStart = timingIsStart;
    }

    public String getTimingCreateTime() {
        return timingCreateTime;
    }

    public void setTimingCreateTime(String timingCreateTime) {
        this.timingCreateTime = timingCreateTime;
    }

    public String getTimingUpdateTime() {
        return timingUpdateTime;
    }

    public void setTimingUpdateTime(String timingUpdateTime) {
        this.timingUpdateTime = timingUpdateTime;
    }
}
