package com.github.aasten.transportconcurrent.events;

public interface EventWithFeedback extends Event {
    EventEnvironmentFeedback getEnvironmentFeedback();
}
