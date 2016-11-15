package com.github.aasten.transportconcurrent.objects;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Attention;

public interface EventEnvironment {
    void subscribeToEvents(Attention attention);
    void unSubscribe(Attention attention);
    void notifyAbout(Event event);
    /**
     * Infinite loop of events processing. Designed to be run in a daemon thread.
     */
    void launchInfinitely();
}
