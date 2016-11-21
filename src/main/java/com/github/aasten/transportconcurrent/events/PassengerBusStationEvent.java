package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Behavior;
import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Station;

public class PassengerBusStationEvent extends FeedingBackEvent implements StationEvent, BusEvent, PassengerEvent{

    public enum EventType {
        PASSENGER_ENTERED_BUS,
        PASSENGER_EXITED_BUS,
    }
    
    private final Passenger passenger;
    private final Bus bus;
    private final Station station;
    private final EventType type;
    private final EventEnvironmentFeedback feedback;
    
    public PassengerBusStationEvent(Passenger passenger, Bus bus, Station station,
            EventType type, EventEnvironmentFeedback feedback)
    {
        this.passenger = passenger;
        this.bus = bus;
        this.station = station;
        this.type = type;
        this.feedback = feedback;
    }
    
    @Override
    public Passenger getPassenger() { return passenger; }

    @Override
    public Bus getBus() { return bus; }

    @Override
    public Station getStation() { return station; }
    
    public EventType getType() { return type; }

    @Override
    public EventEnvironmentFeedback getEnvironmentFeedback() {
        return feedback;
    }

    @Override
    public void affectBehaviorBeforeFeedback(Behavior behavior) {
        behavior.behaveAccording(this);
    }
    
    @Override
    public String toString() {
        String ret = null;
        switch(type) {
        case PASSENGER_ENTERED_BUS:
            ret = "[" + getTimestamp() + "] " + station + ":" + bus + " << " + passenger;
            break;
        case PASSENGER_EXITED_BUS:
            ret = "[" + getTimestamp() + "] " + station + ":" + bus + " >> " + passenger;
            break;
        }
        
        return ret;
    }
    
}
