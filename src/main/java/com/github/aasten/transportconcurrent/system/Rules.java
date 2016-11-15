package com.github.aasten.transportconcurrent.system;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.human.Behavior;
import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.human.PassengerBehaviorAtStation;
import com.github.aasten.transportconcurrent.human.PassengerBehaviorInsideBus;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Station;

public class Rules {
//    public static Behavior<Passenger> passengerBehaviorOutside() {
//        return new Behavior<Passenger>() {
//            public void behaveAccording(Event event, Passenger managed) {
//                // TODO Auto-generated method stub
//                
//            }           
//        }
//    }
    
    public static PassengerBehaviors reactOnEventForPassenger(
            final BusStationEvent event, 
            final Passenger passenger) 
    {
        return basicReaction(event.getBus(), event.getStation(), passenger);
    }
    
    
    public static PassengerBehaviors reactOnEventForPassenger(
            final PassengerBusStationEvent event, 
            final Passenger passenger) 
    {
        return basicReaction(event.getBus(), event.getStation(), passenger);
    }
    
    
    private static PassengerBehaviors basicReaction(
            final Bus bus, final Station station, final Passenger passenger) {
        return new PassengerBehaviors() {
            
            public Behavior getInsideBus() {
                return new PassengerBehaviorInsideBus(bus, passenger);
            }
            
            public Behavior getAtStation() {
                return new PassengerBehaviorAtStation(passenger, station);
            }
        };
    }
    
}
