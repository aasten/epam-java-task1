package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Station;

public class BusStationEvent extends TimestampEvent implements BusEvent, StationEvent {

    public enum EventType {
        BUS_ARRIVED,
        DOORS_OPENED,
        BUS_FULL,
        DOORS_CLOSED,
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
    
    public void accept(EventPool visitor) {
        visitor.visit(this);
    }

    public Station getStation() { return station; }

    public Bus getBus() { return bus; }
    
    public EventType getType() { return type; }

}
