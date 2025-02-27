package com.capstone.jfc.dto.event;

import java.util.UUID;

import com.capstone.jfc.dto.event.payload.AckJobEventPayload;
import com.capstone.jfc.model.EventType;

public class AckTicketUpdateStatusJobEvent implements Event<AckJobEventPayload>{
    private AckJobEventPayload payload;
    private String eventId;
    private EventType type = EventType.ACK_TICKET_UPDATE_STATUS_JOB;


    public AckTicketUpdateStatusJobEvent(AckJobEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }

    public AckTicketUpdateStatusJobEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public AckJobEventPayload getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
