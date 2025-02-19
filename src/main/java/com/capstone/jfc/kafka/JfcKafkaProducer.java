package com.capstone.jfc.kafka;

import com.capstone.jfc.dto.event.payload.ScanRequestEventPayload;
import com.capstone.jfc.model.EventType;
import com.capstone.jfc.model.Job;
import com.capstone.jfc.model.KafkaTopic;

import java.util.List;

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
        String topic = job.getDestTopic().getTopicName();

        // The payload is already a JSON string stored in Job.getPayload().
        // The key is job.getEventId(). The value is job.getPayload().
        
        kafkaTemplate.send(topic, job.getEventId(), job.getPayload());

        // EventType type = job.getEventType();
        // if(type == EventType.SCAN_REQUEST) {
        //     // ScanRequestEventPayload payload = job.getPayload();
        //     System.out.println("3. JFC sent ScanRequestJobEvent id: " + job.getEventId() + "to topic: " + topic);
        // } else if(type == EventType.SCAN_PARSE) {
        //     System.out.println("8. JFC sent ScanParseJobEvent id: " + job.getEventId() + "to topic: " + topic);
        // } else if(type == EventType.STATE_UPDATE) {
        //     System.out.println("JFC sent StateUpdateJobEvent id: " + job.getEventId() + "to topic: " + topic);
        // }

        System.out.println("Sent job to topic: " + topic + " with ID: " + job.getEventId());
    }

    public void sendJobList(List<Job> jobs) {
        for(Job job: jobs) {
            sendEvent(job);
        }
    }

    // private String determineTopic(EventType eventType) {

    //     switch (eventType) {
    //         case SCAN_REQUEST_JOB:
    //             return KafkaTopic.TOOLSCHEDULER_JFC.getTopicName();
    //         case SCAN_PARSE_JOB:
    //             return KafkaTopic.PARSER_JFC.getTopicName();
    //         case STATE_UPDATE_JOB:
    //             return KafkaTopic.BGJOBS_JFC.getTopicName();
    //         case ACK_STATE_UPDATE_JOB:
    //         case ACK_SCAN_REQUEST_JOB:
    //         case ACK_SCAN_PARSE_JOB:
    //         default:
    //             return KafkaTopic.ACK_JOB.getTopicName();
    //     }
    // }
}
