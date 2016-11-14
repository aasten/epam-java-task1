package com.github.aasten.transportconcurrent.events;

// TODO visiting events types in separate interfaces?
public interface EventPool {
    public void visit(BusStationEvent event);
    public void visit(PassengerBusStationEvent event);
}
