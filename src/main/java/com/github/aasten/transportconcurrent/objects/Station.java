package com.github.aasten.transportconcurrent.objects;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Attention;

public class Station implements EventEnvironment {
    
    private final String name; 
    
    public Station(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }

    public void subscribeToEvents(Attention attention) {
        // TODO Auto-generated method stub
        
    }

    public void unSubscribe(Attention attention) {
        // TODO Auto-generated method stub
        
    }

    public void notifyAbout(Event event) {
        // TODO Auto-generated method stub
        
    }

    public void launchInfinitely() {
        // TODO Auto-generated method stub
        
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
