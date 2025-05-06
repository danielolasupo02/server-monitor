package com.ubn_ihs.server_monitor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PowerShellExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PowerShellExecutor.class);
    private static final String POWERSHELL_PATH = "powershell.exe";
    private static final int TIMEOUT_SECONDS = 30;

    /**
     * Execute a PowerShell command
     *
     * @param command The PowerShell command to execute
     * @return List of output lines from the command
     */
    public List<String> executeCommand(String command) {
        List<String> output = new ArrayList<>();
        Process powerShellProcess = null;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(POWERSHELL_PATH, "-Command", command);
            processBuilder.redirectErrorStream(true);

            logger.info("Executing PowerShell command: {}", command);
            powerShellProcess = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.add(line);
                }
            }

            // Wait for the process to complete or timeout
            if (!powerShellProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.error("PowerShell command timed out after {} seconds", TIMEOUT_SECONDS);
                powerShellProcess.destroyForcibly();
                output.add("ERROR: Command timed out after " + TIMEOUT_SECONDS + " seconds");
            }

            int exitCode = powerShellProcess.exitValue();
            if (exitCode != 0) {
                logger.error("PowerShell command failed with exit code: {}", exitCode);
                output.add("ERROR: Command failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error executing PowerShell command", e);
            output.add("ERROR: " + e.getMessage());
            if (powerShellProcess != null) {
                powerShellProcess.destroyForcibly();
            }
            Thread.currentThread().interrupt();
        }

        return output;
    }

    /**
     * Execute a PowerShell command on a remote server using WinRM/PSRemoting
     *
     * @param serverIp IP address of the remote server
     * @param command The PowerShell command to execute on the remote server
     * @return List of output lines from the command
     */
    public List<String> executeRemoteCommand(String serverIp, String command) {
        // Create a PowerShell command to connect to remote server and execute the command
        String remoteCommand = String.format(
                "Invoke-Command -ComputerName %s -ScriptBlock { %s } -ErrorAction Stop",
                serverIp, command);

        return executeCommand(remoteCommand);
    }
}
