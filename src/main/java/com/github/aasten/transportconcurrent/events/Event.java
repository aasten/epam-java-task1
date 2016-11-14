package com.github.aasten.transportconcurrent.events;

import java.util.Date;

public interface Event {
    
    Date getTimestamp();
    
    void accept(EventPool visitor);
        
}
