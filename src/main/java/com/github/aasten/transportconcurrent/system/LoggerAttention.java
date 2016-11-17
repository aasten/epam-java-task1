package com.github.aasten.transportconcurrent.system;

import java.util.Iterator;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Attention;

public class LoggerAttention implements Attention, Iterator<Event> {

    @Override
    public void notifyAbout(Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Event next() {
        // TODO Auto-generated method stub
        return null;
    }

}
