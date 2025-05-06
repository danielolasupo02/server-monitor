package com.ubn_ihs.server_monitor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "server")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serverIp;
    private String serverName;
    private boolean active = true;

    // Default constructor
    public Server() {
    }

    // Constructor with fields
    public Server(String serverIp, String serverName) {
        this.serverIp = serverIp;
        this.serverName = serverName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Server [id=" + id + ", serverIp=" + serverIp + ", serverName=" + serverName + ", active=" + active + "]";
    }
}