package com.capstone.jfc.dto.event;

import java.util.UUID;

import com.capstone.jfc.dto.event.payload.ScanParseJobEventPayload;
import com.capstone.jfc.model.EventType;

public class ScanParseJobEvent implements Event<ScanParseJobEventPayload>{
    private ScanParseJobEventPayload payload;
    private String eventId;
    private EventType type = EventType.SCAN_PARSE_JOB;

    public ScanParseJobEvent(ScanParseJobEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }

    public ScanParseJobEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public ScanParseJobEventPayload getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
