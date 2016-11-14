package com.github.aasten.transportconcurrent.system;

import com.github.aasten.transportconcurrent.events.Event;

public interface Behaviour<T> {
    void behaveAccording(Event event, T managed);
}
