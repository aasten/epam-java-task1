package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.Finish;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;

public class DefaultPassengerBehavior implements Behavior {

    private Passenger passenger;
    
    public DefaultPassengerBehavior(Passenger passenger) {
        this.passenger = passenger;
    }
    
    public void behaveAccording(BusStationEvent event) {
        // no reaction
    }

    public void behaveAccording(PassengerBusStationEvent event) {
        // no reaction
    }

    @Override
    public void behaveAccording(Finish finish) {
        passenger.targetIsAchieved();
    }

}
