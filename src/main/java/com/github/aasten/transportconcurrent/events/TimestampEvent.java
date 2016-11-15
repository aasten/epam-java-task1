package com.github.aasten.transportconcurrent.events;

import java.util.Date;

public abstract class TimestampEvent implements Event {
    private final Date timestamp;
    public TimestampEvent() {
        timestamp = new Date();
    }
    public Date getTimestamp() { return timestamp; }    
}
