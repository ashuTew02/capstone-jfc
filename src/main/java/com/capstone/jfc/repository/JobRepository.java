package com.capstone.jfc.repository;

import com.capstone.jfc.model.EventType;
import com.capstone.jfc.model.Job;
import com.capstone.jfc.model.JobStatus;
import com.capstone.jfc.model.Tool;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByToolAndStatusAndEventType(Tool tool, JobStatus status, EventType eventType);
    int countByStatusAndToolAndEventTypeAndTenantId(JobStatus status, Tool tool, EventType eventType, Long TenantId);
    List<Job> findByStatus(JobStatus status);
    // Optional<Job> findById(Long id);
    // For counting how many are in-progress for a given type+tool+tenant
    long countByStatusAndEventTypeAndTool(JobStatus status, 
                                          EventType eventType, 
                                          Tool tool);

    long countByStatusAndTenantId(JobStatus status, Long tenantId);

    Job findByEventId(String eventId);

        /**
     * Update 'status' for the job row whose event_id = :eventId.
     * If no row is found, zero rows are updated.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE jobs SET status = :status, updated_at = NOW() WHERE event_id = :eventId", 
           nativeQuery = true)
    int updateStatusByEventId(String eventId, String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE jobs SET status = :status, updated_at = NOW() WHERE id = :jobId", 
           nativeQuery = true)
    int updateStatusByJobId(Long jobId, String status);

    
}
