package com.capstone.jfc.kafka;

import com.capstone.jfc.dto.event.AckScanParseJobEvent;
import com.capstone.jfc.dto.event.AckScanRequestJobEvent;
import com.capstone.jfc.dto.event.ScanParseJobEvent;
import com.capstone.jfc.dto.event.ScanRequestJobEvent;
import com.capstone.jfc.dto.event.Event;
import com.capstone.jfc.dto.event.payload.AckJobEventPayload;
import com.capstone.jfc.service.JfcJobService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class JfcKafkaConsumer {

    private final JfcJobService jfcJobService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JfcKafkaConsumer(JfcJobService jfcJobService) {
        this.jfcJobService = jfcJobService;
    }

    @KafkaListener(topics = {"toolscheduler_jfc"}, groupId = "jfc-consumer-group")
    public void onJfcEventReceived(String message) {
        try {
            Event<?> event = decodeEvent(message);

            if (event instanceof ScanParseJobEvent spEvent) {
                System.out.println("6. JFC listens to ScanParseJobEvent on parser topic. id: " + spEvent.getEventId());
                // Create a job in READY status
                jfcJobService.createJobFromEvent(spEvent, spEvent.getPayload().getTool(), spEvent.getPayload().getTenantId());
                System.out.println("7. JFC creates ScanParseJobEvent and saves to db. id: " + spEvent.getEventId());
            }
            // Acks are handled in a separate method
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "authserver_jfc", groupId = "jfc-consumer-group")
    public void onAuthServerEventReceived(String message) {
        try {
            Event<?> event = decodeEvent(message);

            if (event instanceof ScanRequestJobEvent srEvent) {
                System.out.println("2. JFC Received ScanRequestJobEvent id: " + srEvent.getEventId());
                // Create a job in READY status
                jfcJobService.createJobFromEvent(srEvent, srEvent.getPayload().getTool(), srEvent.getPayload().getTenantId());
            }
            // Acks are handled in a separate method
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "ack_job", groupId = "jfc-consumer-group")
    @Transactional
    public void onAckEventReceived(String message) {
        try {
            Event<?> event = decodeEvent(message);

            if (event instanceof AckScanRequestJobEvent ackReqEvent) {
                AckJobEventPayload payload = ackReqEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
                System.out.println("5.1 JFC Received AckScanRequestJobEvent id: " + ackReqEvent.getEventId() + " and sets status to SUCCESS.");
            } else if (event instanceof AckScanParseJobEvent ackParseEvent) {
                AckJobEventPayload payload = ackParseEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
                System.out.println("12. JFC Received AckScanParseJobEvent id: " + ackParseEvent.getEventId() + " and sets status to SUCCESS.");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Event<?> decodeEvent(String json) throws Exception {
        // Basic JSON detection approach (or a more sophisticated one if you have type fields)
        if (json.contains("\"SCAN_REQUEST_JOB\"")) {
            return objectMapper.readValue(json, ScanRequestJobEvent.class);
        } else if (json.contains("\"SCAN_PARSE_JOB\"")) {
            return objectMapper.readValue(json, ScanParseJobEvent.class);
        } else if (json.contains("\"ACK_SCAN_REQUEST_JOB\"")) {
            return objectMapper.readValue(json, AckScanRequestJobEvent.class);
        } else if (json.contains("\"ACK_SCAN_PARSE_JOB\"")) {
            return objectMapper.readValue(json, AckScanParseJobEvent.class);
        }
        throw new IllegalArgumentException("Unknown event type in JSON: " + json);
    }
}
