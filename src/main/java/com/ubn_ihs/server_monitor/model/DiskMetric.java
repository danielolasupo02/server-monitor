package com.ubn_ihs.server_monitor.model;

public class DiskMetric {
    private String serverIp;
    private String serverName;
    private String drive;
    private Double totalGB;
    private Double usedGB;
    private Double freeGB;
    private Double percentUsed;
    private String status;

    // Default constructor
    public DiskMetric() {
    }

    // Constructor with fields
    public DiskMetric(String serverIp, String serverName, String drive, Double totalGB,
                      Double usedGB, Double freeGB, Double percentUsed) {
        this.serverIp = serverIp;
        this.serverName = serverName;
        this.drive = drive;
        this.totalGB = totalGB;
        this.usedGB = usedGB;
        this.freeGB = freeGB;
        this.percentUsed = percentUsed;
        this.status = calculateStatus();
    }

    // Calculate status based on percent used
    private String calculateStatus() {
        if (percentUsed < 70.0) {
            return "Green";
        } else if (percentUsed < 80.0) {
            return "Yellow";
        } else if (percentUsed < 86.0) {
            return "Orange";
        } else {
            return "Red";
        }
    }

    // Getters and Setters
    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getDrive() {
        return drive;
    }

    public void setDrive(String drive) {
        this.drive = drive;
    }

    public Double getTotalGB() {
        return totalGB;
    }

    public void setTotalGB(Double totalGB) {
        this.totalGB = totalGB;
    }

    public Double getUsedGB() {
        return usedGB;
    }

    public void setUsedGB(Double usedGB) {
        this.usedGB = usedGB;
        updatePercentAndStatus();
    }

    public Double getFreeGB() {
        return freeGB;
    }

    public void setFreeGB(Double freeGB) {
        this.freeGB = freeGB;
        updatePercentAndStatus();
    }

    public Double getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(Double percentUsed) {
        this.percentUsed = percentUsed;
        this.status = calculateStatus();
    }

    public String getStatus() {
        return status;
    }

    // Update percent used and status when either used or free GB changes
    private void updatePercentAndStatus() {
        if (totalGB != null && totalGB > 0 && usedGB != null) {
            this.percentUsed = (usedGB / totalGB) * 100;
            this.status = calculateStatus();
        }
    }

    // Get CSS class for status color
    public String getStatusClass() {
        switch (status) {
            case "Green":
                return "status-green";
            case "Yellow":
                return "status-yellow";
            case "Orange":
                return "status-orange";
            case "Red":
                return "status-red";
            default:
                return "";
        }
    }
}
