package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;

public interface Behavior {
    void behaveAccording(BusStationEvent event);
    void behaveAccording(PassengerBusStationEvent event);
}
