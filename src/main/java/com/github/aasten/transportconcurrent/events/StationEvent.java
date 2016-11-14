package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.objects.Station;

public interface StationEvent extends Event {
    Station getStation();    
}
