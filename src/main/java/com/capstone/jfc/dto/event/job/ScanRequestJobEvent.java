package com.capstone.jfc.dto.event.job;

import java.util.UUID;

import com.capstone.jfc.dto.event.Event;
import com.capstone.jfc.dto.event.payload.job.ScanRequestJobEventPayload;
import com.capstone.jfc.model.EventType;

public final class ScanRequestJobEvent implements Event<ScanRequestJobEventPayload> {
    private ScanRequestJobEventPayload payload;
    private String eventId;
    private EventType type = EventType.SCAN_REQUEST_JOB;


    public ScanRequestJobEvent(ScanRequestJobEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }

    public ScanRequestJobEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public ScanRequestJobEventPayload getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
