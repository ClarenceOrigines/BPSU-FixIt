package com.example.bpsufixit;

public class Report {
    public String id;
    public String location;
    public String category;
    public String description;
    public String status;

    public Report() {
        // Empty constructor required by Firebase
    }

    public Report(String id, String location, String category, String description, String status) {
        this.id = id;
        this.location = location;
        this.category = category;
        this.description = description;
        this.status = status;
    }
}
