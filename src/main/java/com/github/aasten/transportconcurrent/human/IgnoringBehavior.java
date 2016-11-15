package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;

public class IgnoringBehavior implements Behavior {

    public void behaveAccording(BusStationEvent event) {
        // no reaction
    }

    public void behaveAccording(PassengerBusStationEvent event) {
        // no reaction
    }

}
