package com.github.aasten.transportconcurrent.human;

import java.util.Collections;
import java.util.Comparator;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Doors;

//TODO replace inheritance with composition?
public class PassengerBehaviorInsideBus extends DefaultPassengerBehavior {
    
    private Bus bus;
    private Passenger passenger;
    
    // TODO optimize: behavior does not depend neither on bus which
    // is provided as constructor parameter but on the by event one.
    public PassengerBehaviorInsideBus(Bus bus, Passenger passenger) {
        super(passenger);
        this.bus = bus;
        this.passenger = passenger;
    }

    public void behaveAccording(BusStationEvent event) {
        switch(event.getType()) {
        case BUS_ARRIVED:
            if(event.getStation().equals(passenger.getDestination())) {
                // find doors with shortest exit queue
                // TODO speedup if choosing first doors with empty queue (for with index)
                Doors doorsToExit = Collections.min(bus.getDoors(), 
                        new Comparator<Doors>() {
                            public int compare(Doors o1, Doors o2) {
                                return o1.exitQueueLength() - o2.exitQueueLength();
                            }
                        } );
                // exit
                doorsToExit.enqueueExit(passenger);
            }
            break;
        default:
            break;
        
        }
    }

    public void behaveAccording(PassengerBusStationEvent event) {
        if(event.getPassenger().equals(passenger)) {
            switch(event.getType()) {
            case PASSENGER_ENTERED_BUS:
                event.getStation().unSubscribe(passenger.getAttention());
                break;
            case PASSENGER_EXITED_BUS:
                event.getBus().unSubscribe(passenger.getAttention());
                if(event.getStation().equals(passenger.getDestination())) {
                    passenger.targetIsAchieved();
                } // else crying XO
                break;
            default:
                break;
            }
        } else {
            // TODO log this, for the optimizing reasons this should not occur
        }
    }

}
