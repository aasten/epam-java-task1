package com.github.aasten.transportconcurrent.events;

public interface EventEnvironmentFeedback {
    // sending feedback that Attention was notified about Event
    void eventWasNoticed(String notificationUID);
}
