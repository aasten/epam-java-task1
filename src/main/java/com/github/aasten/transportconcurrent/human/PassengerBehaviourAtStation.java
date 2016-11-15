package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.system.Behaviour;

public class PassengerBehaviourAtStation extends Behaviour {
    
    private Passenger passenger;
    private Station station;
    
    public PassengerBehaviourAtStation(Passenger passenger, Station station) {
        this.passenger = passenger;
        this.station = station;
    }
    
    @Override
    public void behaveAccording(BusStationEvent event) {
        if(passenger.getDestination().equals(station)) {
            // TODO log this as erroneous creation of passenger with such a destination?
            return;
        }
        switch(event.getType()) {
        case BUS_ARRIVED: 
            break;
        default:
            break;
        }
    }
}
