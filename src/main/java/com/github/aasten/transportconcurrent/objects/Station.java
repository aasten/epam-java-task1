package com.github.aasten.transportconcurrent.objects;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.EventEnvironmentFeedback;
import com.github.aasten.transportconcurrent.events.IncomingEventsProcessing;
import com.github.aasten.transportconcurrent.human.Attention;

public class Station implements EventEnvironment, EventEnvironmentFeedback, IncomingEventsProcessing {
    
    private final String name; 
    private BasicEventProcessing delegateEventProcessing = new BasicEventProcessing();
    private final int busesAtOnce;
    private int busesCurrently = 0;
    
    public Station(String name, int busesAtOnce) {
        this.name = name;
        if(busesAtOnce < 1) {
            LoggerFactory.getLogger(getClass()).warn(
                    "Got buses-at-once value is {}. Used {} instead",busesAtOnce,1);
            busesAtOnce = 1;
        }
        this.busesAtOnce = busesAtOnce;
    }
    
    public Station(String name) {
        this(name,1);
    }
    
    public String name() {
        return name;
    }

    public void subscribeToEvents(Attention attention) {
        delegateEventProcessing.subscribeToEvents(attention);
    }

    public void unSubscribe(Attention attention) {
        delegateEventProcessing.unSubscribe(attention);
    }

    public void notifyAbout(Event event) {
        delegateEventProcessing.notifyAbout(event);
    }

    @Override
    public Runnable getEventProcessor() {
        return delegateEventProcessing;
    }
    
    // equals for stations with same name
    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(getClass())) {
            if(((Station)(obj)).name().equals(name())) {
                return true;
            }
        }
        return false;
    }
    
    void takeBusPlace() {
        synchronized(this) {
            if(busesCurrently < busesAtOnce) {
                busesCurrently++;
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                }
            }
        }
    }
    
    void releaseBusPlace() {
        synchronized(this) {
            if(busesCurrently > 0) {
                busesCurrently--;
            }
            notifyAll();
        }
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public void eventWasNoticedBy(Event event, Object objectNoticed) {
        // TODO Auto-generated method stub
        
    }
}
