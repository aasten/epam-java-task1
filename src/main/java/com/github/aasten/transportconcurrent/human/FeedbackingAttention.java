package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.EventEnvironmentFeedback;

@Deprecated
public interface FeedbackingAttention {
    void notifyAbout(Event event, EventEnvironmentFeedback env);
}
