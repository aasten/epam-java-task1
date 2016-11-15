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
}
