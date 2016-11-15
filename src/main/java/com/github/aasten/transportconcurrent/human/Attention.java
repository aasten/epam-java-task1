package com.github.aasten.transportconcurrent.human;

import java.util.Iterator;

import com.github.aasten.transportconcurrent.events.Event;

public interface Attention {
    void notifyAbout(Event event);
    
    Iterator<Event> eventIterator();
    
}