package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Behavior;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Station;

public class BusStationEvent extends TimestampEvent implements BusEvent, StationEvent {

    public enum EventType {
        BUS_ARRIVED,
        BUS_DEPARTURED,
    }
    
    private final Bus bus;
    private final Station station;
    private final EventType type;
    
    public BusStationEvent(Bus bus, Station station, EventType type) {
        this.bus = bus;
        this.station = station;
        this.type = type;
    }

    public Station getStation() { return station; }

    public Bus getBus() { return bus; }
    
    public EventType getType() { return type; }
    
    
    @Override
    public String toString() {
        String ret = null;
        switch(type) {
        case BUS_ARRIVED:
            ret = "[" + getTimestamp() + "] " + station + " << " + bus;
            break;
        case BUS_DEPARTURED:
            ret = "[" + getTimestamp() + "] " + station + " >> " + bus;
            break;
        }
        return ret;
    }


    @Override
    public void affectBehavior(Behavior behavior) {
        behavior.behaveAccording(this);
    }

}
