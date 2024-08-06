package com.example.hackathonproject;

public class Education {
    private String title;
    private String details;
    private String location;
    private int views;

    public Education(String title, String details, String location, int views) {
        this.title = title;
        this.details = details;
        this.location = location;
        this.views = views;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getLocation() {
        return location;
    }

    public int getViews() {
        return views;
    }
}
