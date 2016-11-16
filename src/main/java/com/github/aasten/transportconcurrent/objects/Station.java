package com.github.aasten.transportconcurrent.objects;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Attention;

public class Station implements EventEnvironment {
    
    private final String name; 
    private EventEnvironment delegateEventProcessing = new BasicEventProcessing();
    
    public Station(String name) {
        this.name = name;
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

    public void launchInfinitely() {
        delegateEventProcessing.launchInfinitely();
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
}
