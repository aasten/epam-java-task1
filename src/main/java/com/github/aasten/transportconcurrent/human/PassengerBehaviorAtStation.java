package com.github.aasten.transportconcurrent.human;

import java.util.Collections;
import java.util.Comparator;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.objects.Doors;
import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.system.Rules;

public class PassengerBehaviorAtStation extends IgnoringBehavior {
    
    private Passenger passenger;
    private Station station;
    
    public PassengerBehaviorAtStation(Passenger passenger, Station station) {
        this.passenger = passenger;
        this.station = station;
        this.station.subscribeToEvents(passenger.getAttention());
    }
    
    @Override
    public void behaveAccording(BusStationEvent event) {
        switch(event.getType()) {
        case BUS_ARRIVED:
            // find doors with shortest enter queue
            // TODO speedup if choosing first doors with empty queue (for with index)
            // TODO Dementra fix 
            Doors doorsToEnter = Collections.min(event.getBus().getDoors(), 
                    new Comparator<Doors>() {
                        public int compare(Doors o1, Doors o2) {
                            return o1.enterQueueLength() - o2.enterQueueLength();
                        }
                    } );
            // try enter
            doorsToEnter.enqueueEnter(passenger);
            station.unSubscribe(passenger.getAttention());
            break;
        default:
            break;
        
        }
//        if(passenger.getDestination().equals(station)) {
//            // TODO log this as erroneous creation of passenger with such a destination?
//            return;
//        }
//        switch(event.getType()) {
//        case BUS_ARRIVED: 
//            break;
//        default:
//            break;
//        }
    }

    public void behaveAccording(PassengerBusStationEvent event) {
        switch(event.getType()) {
        case PASSENGER_EXITED_BUS:
            if(event.getStation().equals(passenger.getDestination())) {
                passenger.targetIsAchieved();
            } // else crying XO
            break;
        case PASSENGER_ENTERED_BUS:
            // TODO Dementra lacks here. Maybe setting whole amount of behaviors 
            // directly to the passenger?
            passenger.setBehavior(Rules.reactOnEventForPassenger(event, passenger).getInsideBus());
            break;
        case PASSENGER_AT_STATION:
            // no behavior changing
            break;
        }
    }
}
