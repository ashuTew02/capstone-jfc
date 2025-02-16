package com.capstone.jfc.service;

import com.capstone.jfc.model.Job;
import com.capstone.jfc.model.JobStatus;
// import com.capstone.jfc.model.Tool;
// import com.capstone.jfc.model.EventType;
import com.capstone.jfc.kafka.JfcKafkaProducer;
import com.capstone.jfc.repository.JobRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SchedulingService {

    private final JobRepository jobRepository;
    private final ConcurrencyConfigService concurrencyConfigService;
    private final JfcKafkaProducer kafkaProducer;

    @Value("${scheduler.interval-in-seconds}")
    private int schedulingIntervalInSeconds;

    public SchedulingService(JobRepository jobRepository,
                             ConcurrencyConfigService concurrencyConfigService,
                             JfcKafkaProducer kafkaProducer) {
        this.jobRepository = jobRepository;
        this.concurrencyConfigService = concurrencyConfigService;
        this.kafkaProducer = kafkaProducer;
    }

    @Scheduled(fixedRateString = "${scheduler.interval-in-seconds}000")
    @Transactional
    public void scheduleJobs() {
        // STEP 1: Load concurrency configs
        ConcurrencyConfigService.ConcurrencyConfigData cfgData = concurrencyConfigService.loadAllConcurrencyConfigs();
        Map<String, Integer> typeToolMap = cfgData.getTypeToolMap(); // e.g. { "SCAN_REQUEST_JOB:code-scan" -> 2 }
        Map<Long, Integer> tenantMap = cfgData.getTenantMap();       // e.g. { 1 -> 3, 2 -> 2 }

        // STEP 2: Fetch all READY jobs
        List<Job> readyJobs = jobRepository.findByStatus(JobStatus.READY);

        // Scheduling
        for (Job job : readyJobs) {
            if (canScheduleJob(job, typeToolMap, tenantMap)) {
                System.out.println("Job scheduled: " + job.toString());
                // Mark as IN_PROGRESS
                job.setStatus(JobStatus.IN_PROGRESS);
                jobRepository.save(job);
                System.out.println("Job set to in progress: " + job.toString());


                // Send event to the appropriate topic
                kafkaProducer.sendEvent(job);
            }
        }
    }

    private boolean canScheduleJob(Job job,
                                   Map<String, Integer> typeToolMap,
                                   Map<Long, Integer> tenantMap) {

        // 1. Check type+tool concurrency
        String typeToolKey = job.getEventType().name() + ":" + job.getTool().getValue();
        System.out.println("TYPE_TOOL_MAP:::::::::::: "+typeToolMap.toString());
        System.out.println("TENANT_MAP:::::::::::: "+tenantMap.toString());
        System.out.println("typeToolKey:::::::::::: "+typeToolKey);                            
        int maxTypeTool = typeToolMap.getOrDefault(typeToolKey, 5); // default = 5 if not set
        long currentCountForTypeTool = jobRepository.countByStatusAndEventTypeAndTool(
                JobStatus.IN_PROGRESS, job.getEventType(), job.getTool());
        if (currentCountForTypeTool >= maxTypeTool) {
            return false;
        }

        // 2. Check tenant concurrency
        Long tenantId = job.getTenantId();
        int maxTenant = tenantMap.getOrDefault(tenantId, 5); // default = 5 if not set
        long currentCountForTenant = jobRepository.countByStatusAndTenantId(JobStatus.IN_PROGRESS, tenantId);
        if (currentCountForTenant >= maxTenant) {
            return false;
        }

        return true;
    }
}
