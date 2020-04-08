package com.r2bd.covid19Helper;

public class VideoModel {
    private  int videoName;
    private  int snapshot;
    private String location;
    private String description;

    public VideoModel(int videoName, int snapshot, String description, String location) {
        this.videoName = videoName;
        this.snapshot = snapshot;
        this.location = location;
        this.description = description;
    }

    public int getVideoName() {
        return videoName;
    }

    public int getSnapshot() {
        return snapshot;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public void setVideoName(int videoName) {
        this.videoName = videoName;
    }

    public void setSnapshot(int snapshot) {
        this.snapshot = snapshot;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
