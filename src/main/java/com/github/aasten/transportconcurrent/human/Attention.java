package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.Event;

public interface Attention {
    void notifyAbout(Event event);
}
