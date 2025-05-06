package com.ubn_ihs.server_monitor.controller;

import com.ubn_ihs.server_monitor.model.Server;
import com.ubn_ihs.server_monitor.service.ServerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ServerController {


    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }



    @GetMapping("/servers")
    public String getAllServers(Model model) {
        List<Server> servers = serverService.getAllServers();

        model.addAttribute("servers", servers);
        model.addAttribute("newServer", new Server());
        return "servers";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Server> getServerById(@PathVariable Long id) {
        Server server = serverService.getServerById(id);
        if (server != null) {
            return new ResponseEntity<>(server, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public String addServer(@ModelAttribute("newServer") Server server, RedirectAttributes redirectAttributes) {
        Server savedServer = serverService.addServer(server);
        redirectAttributes.addFlashAttribute("success", "Server added successfully!");
        return "redirect:/servers";
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Server> updateServer(@PathVariable Long id, @RequestBody Server serverDetails) {
        Server server = serverService.updateServer(id, serverDetails);
        if (server != null) {
            return new ResponseEntity<>(server, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteServer(@PathVariable Long id) {
        boolean deleted = serverService.deleteServer(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}/toggle")
    public String toggleServerActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Server server = serverService.toggleServerActive(id);
        if (server != null) {
            String status = server.isActive() ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "Server " + status + " successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Server not found!");
        }
        return "redirect:/servers";
    }
}
