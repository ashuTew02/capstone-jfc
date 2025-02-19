package com.capstone.jfc.model;

public enum JobType {
    SCAN_REQUEST_JOB,
    SCAN_PARSE_JOB,
    STATE_UPDATE_JOB;

    public EventType toEventType() {
        return switch (this) {
            case SCAN_REQUEST_JOB -> EventType.SCAN_REQUEST_JOB;
            case SCAN_PARSE_JOB -> EventType.SCAN_PARSE_JOB;
            case STATE_UPDATE_JOB -> EventType.STATE_UPDATE_JOB;
        };
    }

    public EventType toAckEventType() {
        return switch (this) {
            case SCAN_REQUEST_JOB -> EventType.ACK_SCAN_REQUEST_JOB;
            case SCAN_PARSE_JOB -> EventType.ACK_SCAN_PARSE_JOB;
            case STATE_UPDATE_JOB -> EventType.ACK_STATE_UPDATE_JOB;
        };
    }
}
