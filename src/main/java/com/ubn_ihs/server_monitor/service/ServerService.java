package com.ubn_ihs.server_monitor.service;

import com.ubn_ihs.server_monitor.model.Server;
import com.ubn_ihs.server_monitor.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServerService {

    @Autowired
    private ServerRepository serverRepository;

    public List<Server> getAllServers() {
        return serverRepository.findAll();
    }

    public List<Server> getAllActiveServers() {
        return serverRepository.findByActiveTrue();
    }

    public Server getServerById(Long id) {
        Optional<Server> server = serverRepository.findById(id);
        return server.orElse(null);
    }

    public Server getServerByIp(String serverIp) {
        return serverRepository.findByServerIp(serverIp);
    }

    public Server addServer(Server server) {
        // Check if server with this IP already exists
        Server existingServer = serverRepository.findByServerIp(server.getServerIp());
        if (existingServer != null) {
            // Update the existing server
            existingServer.setServerName(server.getServerName());
            existingServer.setActive(true);
            return serverRepository.save(existingServer);
        }
        return serverRepository.save(server);
    }

    public Server updateServer(Long id, Server serverDetails) {
        Optional<Server> server = serverRepository.findById(id);
        if (server.isPresent()) {
            Server existingServer = server.get();
            existingServer.setServerName(serverDetails.getServerName());
            existingServer.setServerIp(serverDetails.getServerIp());
            existingServer.setActive(serverDetails.isActive());
            return serverRepository.save(existingServer);
        }
        return null;
    }

    public boolean deleteServer(Long id) {
        Optional<Server> server = serverRepository.findById(id);
        if (server.isPresent()) {
            serverRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Server toggleServerActive(Long id) {
        Optional<Server> server = serverRepository.findById(id);
        if (server.isPresent()) {
            Server existingServer = server.get();
            existingServer.setActive(!existingServer.isActive());
            return serverRepository.save(existingServer);
        }
        return null;
    }
}
