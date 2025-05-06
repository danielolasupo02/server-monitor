package com.ubn_ihs.server_monitor.model;

public class MemoryMetric {
    private String serverIp;
    private String serverName;
    private Double totalMemoryGB;
    private Double freeMemoryGB;
    private Double usedMemoryGB;
    private Double percentUsed;
    private String status;

    // Default constructor
    public MemoryMetric() {
    }

    // Constructor with fields
    public MemoryMetric(String serverIp, String serverName, Double totalMemoryGB,
                        Double freeMemoryGB, Double usedMemoryGB, Double percentUsed) {
        this.serverIp = serverIp;
        this.serverName = serverName;
        this.totalMemoryGB = totalMemoryGB;
        this.freeMemoryGB = freeMemoryGB;
        this.usedMemoryGB = usedMemoryGB;
        this.percentUsed = percentUsed;
        this.status = calculateStatus();
    }

    // Calculate status based on percent used
    private String calculateStatus() {
        if (percentUsed < 40.0) {
            return "Green";
        } else if (percentUsed < 51.0) {
            return "Yellow";
        } else if (percentUsed < 61.0) {
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

    public Double getTotalMemoryGB() {
        return totalMemoryGB;
    }

    public void setTotalMemoryGB(Double totalMemoryGB) {
        this.totalMemoryGB = totalMemoryGB;
        updatePercentAndStatus();
    }

    public Double getFreeMemoryGB() {
        return freeMemoryGB;
    }

    public void setFreeMemoryGB(Double freeMemoryGB) {
        this.freeMemoryGB = freeMemoryGB;
        updatePercentAndStatus();
    }

    public Double getUsedMemoryGB() {
        return usedMemoryGB;
    }

    public void setUsedMemoryGB(Double usedMemoryGB) {
        this.usedMemoryGB = usedMemoryGB;
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

    // Update percent used and status when memory values change
    private void updatePercentAndStatus() {
        if (totalMemoryGB != null && totalMemoryGB > 0
                && (usedMemoryGB != null || (totalMemoryGB != null && freeMemoryGB != null))) {

            // Calculate used memory if needed
            if (usedMemoryGB == null && totalMemoryGB != null && freeMemoryGB != null) {
                this.usedMemoryGB = totalMemoryGB - freeMemoryGB;
            }

            this.percentUsed = (usedMemoryGB / totalMemoryGB) * 100;
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