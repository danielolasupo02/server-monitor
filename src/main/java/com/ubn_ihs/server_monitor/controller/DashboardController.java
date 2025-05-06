package com.ubn_ihs.server_monitor.controller;

import org.springframework.ui.Model;
import com.ubn_ihs.server_monitor.model.DiskMetric;
import com.ubn_ihs.server_monitor.model.MemoryMetric;
import com.ubn_ihs.server_monitor.model.Server;
import com.ubn_ihs.server_monitor.service.MetricCollectionService;
import com.ubn_ihs.server_monitor.service.ServerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class DashboardController {
    private ServerService serverService;
    private MetricCollectionService metricCollectionService;

    public DashboardController(ServerService serverService, MetricCollectionService metricCollectionService) {
        this.serverService = serverService;
        this.metricCollectionService = metricCollectionService;
    }

    @GetMapping
    public String showDashboard(Model model) {
        List<Server> activeServers = serverService.getAllActiveServers();

        // Collect the latest metrics for all active servers if needed
        for (Server server : activeServers) {
            metricCollectionService.collectServerMetrics(server);
        }

        // Get all disk metrics
        Map<String, List<DiskMetric>> allDiskMetrics = metricCollectionService.getAllDiskMetrics();

        // Get all memory metrics
        Map<String, MemoryMetric> allMemoryMetrics = metricCollectionService.getAllMemoryMetrics();

        // Prepare data for the view
        List<DiskMetric> diskMetricsList = new ArrayList<>();
        for (Map.Entry<String, List<DiskMetric>> entry : allDiskMetrics.entrySet()) {
            diskMetricsList.addAll(entry.getValue());
        }

        List<MemoryMetric> memoryMetricsList = new ArrayList<>(allMemoryMetrics.values());

        // Add data to model
        model.addAttribute("servers", activeServers);
        model.addAttribute("diskMetrics", diskMetricsList);
        model.addAttribute("memoryMetrics", memoryMetricsList);

        return "dashboard";
    }

    @GetMapping("/refresh-metrics")
    @ResponseBody
    public Map<String, Object> refreshMetrics() {
        List<Server> activeServers = serverService.getAllActiveServers();

        // Collect metrics for all active servers
        for (Server server : activeServers) {
            metricCollectionService.collectServerMetrics(server);
        }

        // Get all updated metrics
        Map<String, List<DiskMetric>> allDiskMetrics = metricCollectionService.getAllDiskMetrics();
        Map<String, MemoryMetric> allMemoryMetrics = metricCollectionService.getAllMemoryMetrics();

        // Prepare data for the response
        List<DiskMetric> diskMetricsList = new ArrayList<>();
        for (Map.Entry<String, List<DiskMetric>> entry : allDiskMetrics.entrySet()) {
            diskMetricsList.addAll(entry.getValue());
        }

        List<MemoryMetric> memoryMetricsList = new ArrayList<>(allMemoryMetrics.values());

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("diskMetrics", diskMetricsList);
        response.put("memoryMetrics", memoryMetricsList);

        return response;
    }
}