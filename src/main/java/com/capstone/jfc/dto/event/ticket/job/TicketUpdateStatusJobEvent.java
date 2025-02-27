package com.capstone.jfc.dto.event.ticket.job;

import java.util.UUID;

import com.capstone.jfc.dto.event.Event;
import com.capstone.jfc.dto.event.payload.job.TicketUpdateStatusJobEventPayload;
import com.capstone.jfc.model.EventType;

public final class TicketUpdateStatusJobEvent implements Event<TicketUpdateStatusJobEventPayload> {
    private TicketUpdateStatusJobEventPayload payload;
    private String eventId;
    private EventType type = EventType.TICKET_UPDATE_STATUS_JOB;


    public TicketUpdateStatusJobEvent(TicketUpdateStatusJobEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }

    public TicketUpdateStatusJobEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public TicketUpdateStatusJobEventPayload getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
