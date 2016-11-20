package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.objects.EventEnvironment;

public interface EventEnvironmentFeedback extends EventEnvironment {
    // sending feedback that Attention was notified about Event
    void attentionWasNotified(Attention attention, Event event);
}
