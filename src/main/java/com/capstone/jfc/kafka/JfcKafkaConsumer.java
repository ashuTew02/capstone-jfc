package com.capstone.jfc.kafka;

import com.capstone.jfc.dto.event.AckRunbookTriggerJobEvent;
import com.capstone.jfc.dto.event.AckScanParseJobEvent;
import com.capstone.jfc.dto.event.AckScanRequestJobEvent;
import com.capstone.jfc.dto.event.AckStateUpdateJobEvent;
import com.capstone.jfc.dto.event.AckTicketCreateJobEvent;
import com.capstone.jfc.dto.event.AckTicketUpdateStatusJobEvent;
import com.capstone.jfc.dto.event.Event;
import com.capstone.jfc.dto.event.RunbookTriggerEvent;
import com.capstone.jfc.dto.event.ScanParseEvent;
import com.capstone.jfc.dto.event.ScanRequestEvent;
import com.capstone.jfc.dto.event.StateUpdateEvent;
import com.capstone.jfc.dto.event.payload.AckJobEventPayload;
import com.capstone.jfc.dto.event.ticket.TicketCreateEvent;
import com.capstone.jfc.dto.event.ticket.TicketUpdateStatusEvent;
import com.capstone.jfc.model.Job;
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

    @KafkaListener(topics = "#{T(com.capstone.jfc.model.KafkaTopic).JOBINGESTION_JFC.getTopicName()}", groupId = "jfc-consumer-group")   //CHECK IF SOMETHING GOES WRONG...
    public void onEventReceived(String message) {
        // System.out.println("HURRRAYYYYYYYY");
        try {
            Event<?> event = decodeEvent(message);

            if (event instanceof ScanParseEvent spEvent) {
                jfcJobService.createScanParseJobFromEvent(spEvent, spEvent.getPayload().getTool(), spEvent.getPayload().getTenantId(), spEvent.getPayload().getDestTopic());
            } else if (event instanceof ScanRequestEvent srEvent) {
                jfcJobService.createScanRequestJobFromEvent(srEvent, srEvent.getPayload().getTool(), srEvent.getPayload().getTenantId(), srEvent.getPayload().getDestTopic());
            } else if (event instanceof StateUpdateEvent suEvent) {
                System.out.println("SU EVENT:::"+suEvent.getPayload().toString());
                jfcJobService.createStateUpdateJobFromEvent(suEvent, suEvent.getPayload().getTool(), suEvent.getPayload().getTenantId(), suEvent.getPayload().getDestTopic());
            } else if(event instanceof TicketUpdateStatusEvent tuEvent){
                jfcJobService.createTicketUpdateStatusJobFromEvent(tuEvent, tuEvent.getPayload().getTool(), tuEvent.getPayload().getTenantId(), tuEvent.getPayload().getDestTopic());
            } else if(event instanceof TicketCreateEvent tcEvent){
                jfcJobService.createTicketCreateJobFromEvent(tcEvent, tcEvent.getPayload().getTool(), tcEvent.getPayload().getTenantId(), tcEvent.getPayload().getDestTopic());
            } else if(event instanceof RunbookTriggerEvent rbEvent){
                System.out.println("RUNBOOK TRIGGER EVENT:::"+rbEvent.getPayload().toString());
                Job job = jfcJobService.createRunbookTriggerJobFromEvent(rbEvent, rbEvent.getPayload().getTool(), rbEvent.getPayload().getTenantId(), rbEvent.getPayload().getDestTopic());
                System.out.println(job.getPayload().toString());
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
                // Thread.sleep(5000);
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
            } else if (event instanceof AckScanParseJobEvent ackParseEvent) {
                // Thread.sleep(5000);
                AckJobEventPayload payload = ackParseEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
            } else if (event instanceof AckStateUpdateJobEvent ackUpdateEvent) {
                AckJobEventPayload payload = ackUpdateEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
            } else if (event instanceof AckTicketUpdateStatusJobEvent ackTicketUpdateEvent) {
                AckJobEventPayload payload = ackTicketUpdateEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
            } else if (event instanceof AckTicketCreateJobEvent ackTicketCreateEvent) {
                AckJobEventPayload payload = ackTicketCreateEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
            } else if (event instanceof AckRunbookTriggerJobEvent ackRunbookTriggerEvent) {
                AckJobEventPayload payload = ackRunbookTriggerEvent.getPayload();
                jfcJobService.updateJobStatus(payload.getJobId(), payload.getJobStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Event<?> decodeEvent(String json) throws Exception {
        // Basic JSON detection approach (or a more sophisticated one if you have type fields)
        if (json.contains("\"SCAN_REQUEST\"")) {
            return objectMapper.readValue(json, ScanRequestEvent.class);
        } else if (json.contains("\"SCAN_PARSE\"")) {
            return objectMapper.readValue(json, ScanParseEvent.class);
        } else if (json.contains("\"STATE_UPDATE\"")) {
            return objectMapper.readValue(json, StateUpdateEvent.class);
        } 
        
        
        else if (json.contains("\"TICKET_UPDATE_STATUS\"")) {
            return objectMapper.readValue(json, TicketUpdateStatusEvent.class);
        } else if (json.contains("\"TICKET_CREATE\"")) {
            return objectMapper.readValue(json, TicketCreateEvent.class);
        }
        

        else if (json.contains("\"ACK_SCAN_REQUEST_JOB\"")) {
            return objectMapper.readValue(json, AckScanRequestJobEvent.class);
        } else if (json.contains("\"ACK_SCAN_PARSE_JOB\"")) {
            return objectMapper.readValue(json, AckScanParseJobEvent.class);
        } else if (json.contains("\"ACK_STATE_UPDATE_JOB\"")) {
            return objectMapper.readValue(json, AckStateUpdateJobEvent.class);
        } 
        
        
        else if (json.contains("\"ACK_TICKET_UPDATE_STATUS_JOB\"")) {
            return objectMapper.readValue(json, AckTicketUpdateStatusJobEvent.class);
        } else if (json.contains("\"ACK_TICKET_CREATE_JOB\"")) {
            return objectMapper.readValue(json, AckTicketCreateJobEvent.class);
        }

        else if (json.contains("\"RUNBOOK_TRIGGER\"")) {
            return objectMapper.readValue(json, RunbookTriggerEvent.class);
        } else if (json.contains("\"ACK_RUNBOOK_TRIGGER_JOB\"")) {
            return objectMapper.readValue(json, AckRunbookTriggerJobEvent.class);
        }
        throw new IllegalArgumentException("Unknown event type in JSON: " + json);
    }
}
