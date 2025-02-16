package com.capstone.jfc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // We enable scheduling using @EnableScheduling.
    // We'll define our scheduler method in a Service or a dedicated Scheduler class.
}
