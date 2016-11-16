package com.github.aasten.transportconcurrent.human;

import java.util.Collections;
import java.util.Comparator;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Doors;

//TODO replace inheritance with composition?
public class PassengerBehaviorInsideBus extends IgnoringBehavior {
    
    private Bus bus;
    private Passenger passenger;
    
    public PassengerBehaviorInsideBus(Bus bus, Passenger passenger) {
        this.bus = bus;
        this.passenger = passenger;
    }

    public void behaveAccording(BusStationEvent event) {
        switch(event.getType()) {
        case BUS_ARRIVED:
            // find doors with shortest exit queue
            // TODO speedup if choosing first doors with empty queue (for with index)
            // TODO Dementra fix
            Doors doorsToExit = Collections.min(bus.getDoors(), 
                    new Comparator<Doors>() {
                        public int compare(Doors o1, Doors o2) {
                            return o1.exitQueueLength() - o2.exitQueueLength();
                        }
                    } );
            // exit
            doorsToExit.enqueueExit(passenger);
            break;
        default:
            break;
        
        }
    }

    public void behaveAccording(PassengerBusStationEvent event) {
        if(event.getPassenger().equals(passenger)) {
            switch(event.getType()) {
            case PASSENGER_EXITED_BUS:
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
