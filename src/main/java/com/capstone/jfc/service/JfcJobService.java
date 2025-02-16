package com.capstone.jfc.service;

import com.capstone.jfc.model.Job;
import com.capstone.jfc.dto.event.Event;
import com.capstone.jfc.model.JobStatus;
import com.capstone.jfc.model.Tool;
import com.capstone.jfc.repository.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.UUID;

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
    public Job createJobFromEvent(Event<?> event, Tool tool, Long tenantId) {
        Job job = new Job();
        job.setEventId(event.getEventId());
        job.setEventType(event.getType());
        job.setTool(tool);
        job.setTenantId(tenantId);
        job.setStatus(JobStatus.READY);

        try {
            String payloadString = objectMapper.writeValueAsString(event);
            job.setPayload(payloadString);
            System.out.println("Job Created from event at JFC Job Service: " + payloadString);
        } catch (JsonProcessingException e) {
            // In production, handle properly or throw a custom exception
            job.setPayload("{}");
        }

        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJobStatus(String eventId, JobStatus status) {
        // 1) Manually update the row in DB (no insertion).
        int updatedCount = jobRepository.updateStatusByEventId(eventId, status.name());

        if (updatedCount == 0) {
            // No existing job found with that eventId to update
            System.out.println("No existing job found for eventId=" + eventId 
                               + ". Cannot update status to " + status);
            return null;
        }

        // 2) If we want the updated Job object for further logic, fetch it now:
        Job updatedJob = jobRepository.findByEventId(eventId);
        System.out.println("Job updated. ID=" + updatedJob.getId() 
                           + " new status=" + updatedJob.getStatus());
        return updatedJob;
    }
}
