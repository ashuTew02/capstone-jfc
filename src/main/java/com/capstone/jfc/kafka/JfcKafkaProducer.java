package com.capstone.jfc.kafka;

import com.capstone.jfc.model.EventType;
import com.capstone.jfc.model.Job;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class JfcKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public JfcKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Job job) {
        // Decide topic (example logic: SCAN_REQUEST_JOB -> "toolscheduler_jfc", SCAN_PARSE_JOB -> "parser_jfc", ACK -> "ack_job")
        String topic = determineTopic(job.getEventType());

        // The payload is already a JSON string stored in Job.getPayload().
        // The key is job.getEventId(). The value is job.getPayload().
        kafkaTemplate.send(topic, job.getEventId(), job.getPayload());
        EventType type = job.getEventType();
        if(type == EventType.SCAN_REQUEST_JOB) {
            System.out.println("3. JFC sent ScanRequestJobEvent id: " + job.getEventId() + "to topic: " + topic);
        } else if(type == EventType.SCAN_PARSE_JOB) {
            System.out.println("8. JFC sent ScanParseJobEvent id: " + job.getEventId() + "to topic: " + topic);
        }

        System.out.println("Sent job to topic: " + topic + " with ID: " + job.getEventId());
    }

    private String determineTopic(EventType eventType) {
        // Adjust to match your real routing:
        switch (eventType) {
            case SCAN_REQUEST_JOB:
                return "toolscheduler_jfc";
            case SCAN_PARSE_JOB:
                return "parser_jfc";
            case ACK_SCAN_REQUEST_JOB:
            case ACK_SCAN_PARSE_JOB:
            default:
                return "ack_job";
        }
    }
}
