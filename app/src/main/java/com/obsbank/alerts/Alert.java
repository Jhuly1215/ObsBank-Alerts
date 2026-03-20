package com.obsbank.alerts;

public class Alert {
    private String title;
    private String body;
    private String severity;
    private long timestamp;
    private boolean isExpanded;

    public Alert(String title, String body, String severity, long timestamp) {
        this.title = title;
        this.body = body;
        this.severity = severity;
        this.timestamp = timestamp;
        this.isExpanded = false;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getSeverity() { return severity; }
    public long getTimestamp() { return timestamp; }
    
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
