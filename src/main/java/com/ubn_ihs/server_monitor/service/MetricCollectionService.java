package com.ubn_ihs.server_monitor.service;


import com.ubn_ihs.server_monitor.model.DiskMetric;
import com.ubn_ihs.server_monitor.model.MemoryMetric;
import com.ubn_ihs.server_monitor.model.Server;
import com.ubn_ihs.server_monitor.util.PowerShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MetricCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectionService.class);

    private final ServerService serverService;
    private final PowerShellExecutor powerShellExecutor;

    // Constructor for dependency injection
    public MetricCollectionService(ServerService serverService, PowerShellExecutor powerShellExecutor) {
        this.serverService = serverService;
        this.powerShellExecutor = powerShellExecutor;
    }

    // In-memory cache for collected metrics
    private final Map<String, List<DiskMetric>> diskMetricsCache = new ConcurrentHashMap<>();
    private final Map<String, MemoryMetric> memoryMetricsCache = new ConcurrentHashMap<>();

    /**
     * Scheduled job to collect metrics from all active servers
     */
    @Scheduled(fixedRateString = "${metrics.collection.interval:60000}")
    public void collectAllServerMetrics() {
        logger.info("Starting scheduled metrics collection");
        List<Server> activeServers = serverService.getAllActiveServers();

        for (Server server : activeServers) {
            try {
                collectServerMetrics(server);
            } catch (Exception e) {
                logger.error("Error collecting metrics for server {}: {}", server.getServerIp(), e.getMessage());
            }
        }

        logger.info("Completed scheduled metrics collection for {} servers", activeServers.size());
    }

    /**
     * Collect metrics for a specific server
     */
    public void collectServerMetrics(Server server) {
        collectDiskMetrics(server);
        collectMemoryMetrics(server);
    }

    /**
     * Get all collected disk metrics
     */
    public Map<String, List<DiskMetric>> getAllDiskMetrics() {
        return new HashMap<>(diskMetricsCache);
    }

    /**
     * Get all collected memory metrics
     */
    public Map<String, MemoryMetric> getAllMemoryMetrics() {
        return new HashMap<>(memoryMetricsCache);
    }

    /**
     * Get disk metrics for a specific server
     */
    public List<DiskMetric> getDiskMetricsForServer(String serverIp) {
        return diskMetricsCache.getOrDefault(serverIp, new ArrayList<>());
    }

    /**
     * Get memory metrics for a specific server
     */
    public MemoryMetric getMemoryMetricsForServer(String serverIp) {
        return memoryMetricsCache.get(serverIp);
    }

    /**
     * Collect disk metrics for a server
     */
    private void collectDiskMetrics(Server server) {
        String serverIp = server.getServerIp();
        String serverName = server.getServerName();

        // PowerShell command to get disk information
        String diskCommand = "Get-PSDrive -PSProvider FileSystem | Select-Object Name, @{Name='TotalGB';Expression={[math]::Round($_.Used/1GB + $_.Free/1GB, 2)}}, @{Name='UsedGB';Expression={[math]::Round($_.Used/1GB, 2)}}, @{Name='FreeGB';Expression={[math]::Round($_.Free/1GB, 2)}}, @{Name='PercentUsed';Expression={[math]::Round($_.Used/($_.Used + $_.Free) * 100, 2)}} | ConvertTo-Json";

        List<String> output;
        if (isLocalhost(serverIp)) {
            output = powerShellExecutor.executeCommand(diskCommand);
        } else {
            output = powerShellExecutor.executeRemoteCommand(serverIp, diskCommand);
        }

        List<DiskMetric> diskMetrics = parseDiskOutput(output, serverIp, serverName);
        if (!diskMetrics.isEmpty()) {
            diskMetricsCache.put(serverIp, diskMetrics);
        }
    }

    /**
     * Collect memory metrics for a server
     */
    private void collectMemoryMetrics(Server server) {
        String serverIp = server.getServerIp();
        String serverName = server.getServerName();

        // PowerShell command to get memory information
        String memoryCommand = "Get-CimInstance Win32_OperatingSystem | Select-Object @{Name='TotalMemoryGB';Expression={[math]::Round($_.TotalVisibleMemorySize/1MB, 2)}}, @{Name='FreeMemoryGB';Expression={[math]::Round($_.FreePhysicalMemory/1MB, 2)}}, @{Name='UsedMemoryGB';Expression={[math]::Round(($_.TotalVisibleMemorySize - $_.FreePhysicalMemory)/1MB, 2)}}, @{Name='PercentUsed';Expression={[math]::Round(($_.TotalVisibleMemorySize - $_.FreePhysicalMemory)/$_.TotalVisibleMemorySize * 100, 2)}} | ConvertTo-Json";

        List<String> output;
        if (isLocalhost(serverIp)) {
            output = powerShellExecutor.executeCommand(memoryCommand);
        } else {
            output = powerShellExecutor.executeRemoteCommand(serverIp, memoryCommand);
        }

        MemoryMetric memoryMetric = parseMemoryOutput(output, serverIp, serverName);
        if (memoryMetric != null) {
            memoryMetricsCache.put(serverIp, memoryMetric);
        }
    }

    /**
     * Parse the disk metrics from PowerShell output
     */
    private List<DiskMetric> parseDiskOutput(List<String> output, String serverIp, String serverName) {
        List<DiskMetric> diskMetrics = new ArrayList<>();

        if (output == null || output.isEmpty()) {
            logger.warn("No disk output received for server: {}", serverIp);
            return diskMetrics;
        }

        // Join all lines and parse the JSON
        StringBuilder jsonBuilder = new StringBuilder();
        for (String line : output) {
            jsonBuilder.append(line);
        }

        String json = jsonBuilder.toString();

        // Simple regex-based parsing (in a production app, you'd use a proper JSON parser)
        Pattern pattern = Pattern.compile("\\{[^}]*\"Name\"\\s*:\\s*\"([^\"]+)\"[^}]*\"TotalGB\"\\s*:\\s*([\\d.]+)[^}]*\"UsedGB\"\\s*:\\s*([\\d.]+)[^}]*\"FreeGB\"\\s*:\\s*([\\d.]+)[^}]*\"PercentUsed\"\\s*:\\s*([\\d.]+)[^}]*\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            try {
                String drive = matcher.group(1);
                double totalGB = Double.parseDouble(matcher.group(2));
                double usedGB = Double.parseDouble(matcher.group(3));
                double freeGB = Double.parseDouble(matcher.group(4));
                double percentUsed = Double.parseDouble(matcher.group(5));

                DiskMetric diskMetric = new DiskMetric(serverIp, serverName, drive, totalGB, usedGB, freeGB, percentUsed);
                diskMetrics.add(diskMetric);
            } catch (NumberFormatException e) {
                logger.error("Error parsing disk metrics for server {}: {}", serverIp, e.getMessage());
            }
        }

        return diskMetrics;
    }

    /**
     * Parse the memory metrics from PowerShell output
     */
    private MemoryMetric parseMemoryOutput(List<String> output, String serverIp, String serverName) {
        if (output == null || output.isEmpty()) {
            logger.warn("No memory output received for server: {}", serverIp);
            return null;
        }

        // Join all lines and parse the JSON
        StringBuilder jsonBuilder = new StringBuilder();
        for (String line : output) {
            jsonBuilder.append(line);
        }

        String json = jsonBuilder.toString();

        // Simple regex-based parsing
        Pattern pattern = Pattern.compile("\\{[^}]*\"TotalMemoryGB\"\\s*:\\s*([\\d.]+)[^}]*\"FreeMemoryGB\"\\s*:\\s*([\\d.]+)[^}]*\"UsedMemoryGB\"\\s*:\\s*([\\d.]+)[^}]*\"PercentUsed\"\\s*:\\s*([\\d.]+)[^}]*\\}");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            try {
                double totalMemoryGB = Double.parseDouble(matcher.group(1));
                double freeMemoryGB = Double.parseDouble(matcher.group(2));
                double usedMemoryGB = Double.parseDouble(matcher.group(3));
                double percentUsed = Double.parseDouble(matcher.group(4));

                return new MemoryMetric(serverIp, serverName, totalMemoryGB, freeMemoryGB, usedMemoryGB, percentUsed);
            } catch (NumberFormatException e) {
                logger.error("Error parsing memory metrics for server {}: {}", serverIp, e.getMessage());
            }
        }

        return null;
    }

    /**
     * Check if the IP represents localhost
     */
    private boolean isLocalhost(String ip) {
        return ip.equals("localhost") || ip.equals("127.0.0.1") || ip.equals("::1");
    }}
