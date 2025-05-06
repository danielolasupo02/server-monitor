package com.ubn_ihs.server_monitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // This class enables Spring's scheduling capabilities
    // The actual scheduling is defined in the MetricCollectionService
}