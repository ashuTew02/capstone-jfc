package com.capstone.jfc.dto.event;

import com.capstone.jfc.model.EventType;

public interface Event<T> {
    EventType getType();
    T getPayload();
    String getEventId();
}
