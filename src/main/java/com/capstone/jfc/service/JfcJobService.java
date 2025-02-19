package com.capstone.jfc.service;

import com.capstone.jfc.model.EventType;
import com.capstone.jfc.model.Job;
import com.capstone.jfc.dto.event.Event;
import com.capstone.jfc.dto.event.ScanParseEvent;
import com.capstone.jfc.dto.event.ScanRequestEvent;
import com.capstone.jfc.dto.event.StateUpdateEvent;
import com.capstone.jfc.dto.event.job.ScanParseJobEvent;
import com.capstone.jfc.dto.event.job.ScanRequestJobEvent;
import com.capstone.jfc.dto.event.job.StateUpdateJobEvent;
import com.capstone.jfc.dto.event.payload.ScanParseEventPayload;
import com.capstone.jfc.dto.event.payload.ScanRequestEventPayload;
import com.capstone.jfc.dto.event.payload.StateUpdateEventPayload;
import com.capstone.jfc.dto.event.payload.job.ScanParseJobEventPayload;
import com.capstone.jfc.dto.event.payload.job.ScanRequestJobEventPayload;
import com.capstone.jfc.dto.event.payload.job.StateUpdateJobEventPayload;
import com.capstone.jfc.model.JobStatus;
import com.capstone.jfc.model.KafkaTopic;
import com.capstone.jfc.model.Tool;
import com.capstone.jfc.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JfcJobService {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public JfcJobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new Job from an incoming event and persist it.
     */
    public Job createScanRequestJobFromEvent(ScanRequestEvent event, Tool tool, Long tenantId, KafkaTopic destTopic) {
        Job job = new Job();
        job.setTool(tool);
        job.setTenantId(tenantId);
        job.setStatus(JobStatus.READY);
        job.setDestTopic(destTopic);
        job.setEventType(EventType.SCAN_REQUEST_JOB);

        job.setEventId(event.getEventId());
        Job savedJob = jobRepository.save(job);
        ScanRequestEventPayload payload = event.getPayload();
        ScanRequestJobEventPayload newPayload = new ScanRequestJobEventPayload(savedJob.getId(), payload);
        ScanRequestJobEvent newEvent = new ScanRequestJobEvent(newPayload);
        savedJob.setEventId(newEvent.getEventId());

        try {
            String payloadString = objectMapper.writeValueAsString(newEvent);
            savedJob.setPayload(payloadString);
            System.out.println("Job Created from event at JFC Job Service: " + payloadString);
        } catch (JsonProcessingException e) {
            // In production, handle properly or throw a custom exception
            savedJob.setPayload("{}");
            System.out.println(e.getMessage());
        }

        return jobRepository.save(savedJob);
    }

    public Job createScanParseJobFromEvent(ScanParseEvent event, Tool tool, Long tenantId, KafkaTopic destTopic) {
        Job job = new Job();
        job.setTool(tool);
        job.setTenantId(tenantId);
        job.setStatus(JobStatus.READY);
        job.setDestTopic(destTopic);
        job.setEventId(event.getEventId());
        job.setEventType(EventType.SCAN_PARSE_JOB);
        Job savedJob = jobRepository.save(job);
        
        ScanParseEventPayload payload = event.getPayload();
        ScanParseJobEventPayload newPayload = new ScanParseJobEventPayload(savedJob.getId(), payload);
        ScanParseJobEvent newEvent = new ScanParseJobEvent(newPayload);
        job.setEventId(newEvent.getEventId());

        try {
            String payloadString = objectMapper.writeValueAsString(newEvent);
            savedJob.setPayload(payloadString);
            System.out.println("Job Created from event at JFC Job Service: " + payloadString);
        } catch (JsonProcessingException e) {
            // In production, handle properly or throw a custom exception
            savedJob.setPayload("{}");
            System.out.println(e.getMessage());
        }

        return jobRepository.save(savedJob);
    }

    public Job createStateUpdateJobFromEvent(StateUpdateEvent event, Tool tool, Long tenantId, KafkaTopic destTopic) {
        System.out.println("IN FUNCCCC");
        Job job = new Job();
        job.setTool(tool);
        job.setTenantId(tenantId);
        job.setStatus(JobStatus.READY);
        job.setDestTopic(destTopic);
        job.setEventId(event.getEventId());
        job.setEventType(EventType.STATE_UPDATE_JOB);
        Job savedJob = jobRepository.save(job);
        StateUpdateEventPayload payload = event.getPayload();
        StateUpdateJobEventPayload newPayload = new StateUpdateJobEventPayload(savedJob.getId(), payload);
        StateUpdateJobEvent newEvent = new StateUpdateJobEvent(newPayload);
        job.setEventId(newEvent.getEventId());

        try {
            String payloadString = objectMapper.writeValueAsString(newEvent);
            savedJob.setPayload(payloadString);
            System.out.println("Job Created from event at JFC Job Service: " + payloadString);
        } catch (JsonProcessingException e) {
            // In production, handle properly or throw a custom exception
            savedJob.setPayload("{}");
            System.out.println(e.getMessage());
        }

        return jobRepository.save(savedJob);
    }

    @Transactional
    public void updateJobStatus(Long jobId, JobStatus status) {
        // 1) Manually update the row in DB (no insertion).
        int updatedCount = jobRepository.updateStatusByJobId(jobId, status.name());

        if (updatedCount == 0) {
            // No existing job found with that eventId to update
            System.out.println("No existing job found for jobId=" + jobId 
                               + ". Cannot update status to " + status);
            // return null;
        }

        // // 2) If we want the updated Job object for further logic, fetch it now:
        // Optional<Job> jobOpt = jobRepository.findById(jobId);
        // if (jobOpt.isEmpty()) {
        //     return null;    // throw exception
        // }

        // User user = userOpt.get();
        // Job updatedJob = jobRepository.findById(jobId);
        // System.out.println("Job updated. ID=" + updatedJob.getId() 
        //                    + " new status=" + updatedJob.getStatus());
        // return updatedJob;
    }

    @Transactional
    public void updateJobListStatus(List<Job> jobs, JobStatus status) {

        for(Job job: jobs) {
            Long jobId = job.getId();
            updateJobStatus(jobId, status);
            // updateJobStatus(eventId, status)

        }
    }
}
