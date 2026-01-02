package com.example.gateway.model;

public class BackendServer {

    private final String url;
    private volatile boolean healthy = true;  // assume healthy on startup
    private int consecutiveFailures = 0;
    private int consecutiveSuccesses = 0;

    public BackendServer(String url) {
        this.url = url;
    }

    public String getUrl() { return url; }
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public int getConsecutiveSuccesses() { return consecutiveSuccesses; }
    public void setConsecutiveSuccesses(int consecutiveSuccesses) { this.consecutiveSuccesses = consecutiveSuccesses; }
}
