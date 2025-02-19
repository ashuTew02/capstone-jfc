package com.capstone.jfc.service;

import com.capstone.jfc.model.EventType;
import com.capstone.jfc.model.Job;
import com.capstone.jfc.model.JobStatus;
import com.capstone.jfc.model.JobType;
import com.capstone.jfc.model.Tool;
// import com.capstone.jfc.model.EventType;
import com.capstone.jfc.kafka.JfcKafkaProducer;
import com.capstone.jfc.repository.JobRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SchedulingService {

    private final JobRepository jobRepository;
    private final ConcurrencyConfigService concurrencyConfigService;
    private final JfcKafkaProducer kafkaProducer;
    private final JfcJobService jobService;

    @Value("${scheduler.interval-in-seconds}")
    private int schedulingIntervalInSeconds;
    private final Object schedulingLock = new Object();

    public SchedulingService(JobRepository jobRepository,
                             ConcurrencyConfigService concurrencyConfigService,
                             JfcKafkaProducer kafkaProducer,
                             JfcJobService jobService) {
        this.jobRepository = jobRepository;
        this.concurrencyConfigService = concurrencyConfigService;
        this.kafkaProducer = kafkaProducer;
        this.jobService = jobService;
    }

    @Scheduled(fixedRateString = "${scheduler.interval-in-seconds}000")
    public void scheduleJobsPeriodically() {
        scheduleJobs();
    }

    @Transactional
    public void scheduleJobs() {
        synchronized (schedulingLock)
        {

            ConcurrencyConfigService.ConcurrencyConfigData cfgData = concurrencyConfigService.loadAllConcurrencyConfigs();
            Map<String, Integer> typeToolMap = cfgData.getTypeToolMap(); // e.g. { "SCAN_REQUEST_JOB:code-scan" -> 2 }
            Map<Long, Integer> tenantMap = cfgData.getTenantMap(); 

            for(Tool tool: Tool.values()) {
                for(JobType jobType: JobType.values()) {
                    EventType eventType = jobType.toEventType();
                    List<Job> jobsToBeScheduled = scheduleJobs(tool, eventType, typeToolMap, tenantMap);
                    if (jobsToBeScheduled.isEmpty()) continue;
                    jobService.updateJobListStatus(jobsToBeScheduled, JobStatus.IN_PROGRESS);
                    kafkaProducer.sendJobList(jobsToBeScheduled);
                }
            }
        }
    }

    public List<Job> scheduleJobs(Tool tool, EventType eventType, Map<String, Integer> typeToolMap, Map<Long, Integer> tenantMap) {
        
        // 1) concurrency limit for the job type
        String jobTypeKey = eventType.name() + ":" + tool.getValue();
        int typeToolLimit;
        try {
            typeToolLimit = typeToolMap.get(jobTypeKey);
        } catch (Exception e) {
            System.out.println("No Config available for tool: " + tool.getValue() + "and eventType: " + eventType);
            return new ArrayList<>();
        }

        long inProgressCount = jobRepository.countByStatusAndEventTypeAndTool(JobStatus.IN_PROGRESS, eventType, tool);
        int availableSlots = typeToolLimit - (int)inProgressCount;
        if (availableSlots <= 0) {
            return Collections.emptyList();
        }

        List<Job> readyJobs = jobRepository.findByToolAndStatusAndEventType(tool, JobStatus.READY, eventType);
        if (readyJobs.isEmpty()) return new ArrayList<>();
        
        List<Job> scheduled = new ArrayList<>();
        Map<Long, Long> tenantInProgressMap = new HashMap<>();
        for (Job j : readyJobs) {
            if (scheduled.size() >= availableSlots) break;

            long tenantCount = jobRepository.countByStatusAndToolAndEventTypeAndTenantId(JobStatus.IN_PROGRESS, tool, eventType, j.getTenantId());
            tenantInProgressMap.putIfAbsent(j.getTenantId(), tenantCount);
            
            int tenantLimit;
            try {
                tenantLimit = tenantMap.get(j.getTenantId());
            } catch (Exception e) {
                System.out.println("No Config Available for tenantId: " + j.getTenantId());
                return new ArrayList<>();
            }
            if (tenantInProgressMap.get(j.getTenantId()) < tenantLimit) {
                // schedule
                j.setStatus(JobStatus.READY);
                // j.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(j);
                scheduled.add(j);

                // occupant
                tenantInProgressMap.put(j.getTenantId(), tenantInProgressMap.get(j.getTenantId()) + 1);
            }
        }
        return scheduled;
    }

}
