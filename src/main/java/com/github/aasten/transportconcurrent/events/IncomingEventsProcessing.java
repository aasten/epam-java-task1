package com.github.aasten.transportconcurrent.events;

public interface IncomingEventsProcessing {
    Runnable getEventProcessor();
}
