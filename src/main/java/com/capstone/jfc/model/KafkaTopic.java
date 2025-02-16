package com.capstone.jfc.model;

public enum KafkaTopic {
    TOOLSCHEDULER_JFC("toolscheduler_jfc"),
    AUTHSERVER_JFC("authserver_jfc"),
    PARSER_JFC("parser_jfc"),
    ACK_JOB("ack_job");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
