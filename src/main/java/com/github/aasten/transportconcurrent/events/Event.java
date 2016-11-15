package com.github.aasten.transportconcurrent.events;

import java.util.Date;

import com.github.aasten.transportconcurrent.human.Behavior;

public interface Event {
    
    Date getTimestamp();
    
    void accept(EventPool visitor);
    
    void affectBehavior(Behavior behavior);
        
}
