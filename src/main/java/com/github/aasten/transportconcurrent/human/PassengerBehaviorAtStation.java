package com.github.aasten.transportconcurrent.human;

import java.util.Collections;
import java.util.Comparator;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.objects.Doors;
import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.system.Rules;

// TODO replace inheritance with composition?
// Behavior having main aim to enter the bus
public class PassengerBehaviorAtStation extends DefaultPassengerBehavior {
    
    private Passenger passenger;
    
    public PassengerBehaviorAtStation(Passenger passenger, Station station) {
        super(passenger);
        this.passenger = passenger;
        station.subscribeToEvents(passenger.getAttention());
    }
    
    @Override
    public void behaveAccording(BusStationEvent event) {
        switch(event.getType()) {
        case BUS_ARRIVED:
            // find doors with shortest enter queue
            // TODO speedup if choosing first doors with empty queue (for with index)
            // TODO Dementra fix? ( event.getBus().getDoors() )
            Doors doorsToEnter = Collections.min(event.getBus().getDoors(), 
                    new Comparator<Doors>() {
                        public int compare(Doors o1, Doors o2) {
                            return o1.enterQueueLength() - o2.enterQueueLength();
                        }
                    } );
            // try enter
            doorsToEnter.enqueueEnter(passenger);
            event.getEnvironmentFeedback().eventWasNoticedBy(event, passenger);
            break;
        default:
            break;
        
        }
    }

    public void behaveAccording(PassengerBusStationEvent event) {
        if(event.getPassenger().equals(passenger)) {
            switch(event.getType()) {
            case PASSENGER_ENTERED_BUS:
                // TODO Dementra lacks here. Maybe setting whole amount of behaviors 
                // directly to the passenger?
                event.getStation().unSubscribe(passenger.getAttention());
                event.getBus().subscribeToEvents(passenger.getAttention());            
                passenger.setBehavior(Rules.reactOnEventForPassenger(event, passenger).getInsideBus());
                break;
            default:
                break;
            }
        }
    }
}
