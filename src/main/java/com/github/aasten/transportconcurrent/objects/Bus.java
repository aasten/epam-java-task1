package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.IncomingEventsProcessing;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent.EventType;
import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.objects.Route.RouteElement;

// TODO seems to have to many tasks
public class Bus implements EventEnvironment, IncomingEventsProcessing {

    private static final long WAIT_PASSENGERS_AT_DOORS_MSEC = 1000;
    
    
    private final int capacity;
    private int currentPlacesTaken = 0;
    private final List<Doors> doors;
    private Station currentStation;
    private Route route;
    private volatile double averageSpeedMeterPerSec;
    private double initialDelay;
    private BasicEventProcessing delegateEventProcessing = new BasicEventProcessing();
    // waiting for queues populated after arriving before entering/exiting
    private final Object passengerEntering = new Object();
    private final String busId;
    
    public Bus(String id, int capacity, int doorsCount, Route route, double averSpeedMeterPerSec,
            double atFirstStationAfterSeconds) {
        busId = id;
        this.capacity = capacity;
        if(doorsCount < 1) {
            // TODO log this
            doorsCount = 1;
        }
        doors = Collections.unmodifiableList(createDoorsList(doorsCount, this));
        this.route = route;
        this.averageSpeedMeterPerSec = averSpeedMeterPerSec;
        initialDelay = atFirstStationAfterSeconds;
    }
    
    public void setAverageSpeed(double metersPerSeconds) {
        this.averageSpeedMeterPerSec = metersPerSeconds;
    }
    
    private static List<Doors> createDoorsList(int doorsCount, Bus bus) {
        List<Doors> list = new ArrayList<Doors>(doorsCount);
        for(int i = 0; i < doorsCount; ++i) {
            list.add(new Doors(bus));
        }
        return list;
    }
    
    public boolean isFull() {
        synchronized(passengerEntering) {
            return currentPlacesTaken >= capacity;
        }
    }
    
    boolean enter(Passenger passenger) {
        synchronized(passengerEntering) {
            if(!isFull()) {
                currentPlacesTaken++;
                // notify passenger through own event environment
                Event passengerEntered = new PassengerBusStationEvent(
                        passenger, this, currentStation, 
                        EventType.PASSENGER_ENTERED_BUS);
                currentStation.notifyAbout(passengerEntered);
                return true;
            } else {
                return false;
            }
        }
    }
    
    private void openAllDoors() {
        for(Doors doors : doors) {
            doors.open();
        }
    }
    private void closeAllDoors() {
        for(Doors doors : doors) {
            doors.close();
        }
    }
    
    
    void exit(Passenger passenger) {
        synchronized(passengerEntering) {
            if(currentPlacesTaken > 0 ) {
                currentPlacesTaken--;
            }
            notifyAbout(new PassengerBusStationEvent(
                    passenger, this, currentStation, 
                    EventType.PASSENGER_EXITED_BUS));
        }
    }
    
    public List<Doors> getDoors() {
        return doors;
    }

    public void subscribeToEvents(Attention attention) {
        delegateEventProcessing.subscribeToEvents(attention);
    }

    public void unSubscribe(Attention attention) {
        delegateEventProcessing.unSubscribe(attention);
    }

    public void notifyAbout(Event event) {
        // will wait until all the attentions are notified
        delegateEventProcessing.notifyAbout(event); 
    }

    @Override
    public Runnable getEventProcessor() {
        return delegateEventProcessing;
    }
    
    
    // main function
    public void walkingTheRoute() {
        Iterator<RouteElement> routeIterator = route.getFirst();
        if(routeIterator.hasNext()) {
            try {
                Thread.sleep((long)(1000*initialDelay));
                do {
                    final RouteElement r = routeIterator.next();
                    Thread.sleep((long)(1000*r.distanceMeters()/averageSpeedMeterPerSec));
                    currentStation = r.nextStation();
                    // wait for free place for bus if busy
                    currentStation.takeBusPlace();
                    Event arriving = new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_ARRIVED);
                    this.notifyAbout(arriving);
                    currentStation.notifyAbout(arriving);
                    openAllDoors();
                    // wait for passengers to fill queues for exit and enter
                    Thread.sleep(WAIT_PASSENGERS_AT_DOORS_MSEC);
                    // will close after single iteration for processing queues
                    // which has been formed for this time
                    // TODO infinite cycle may be here if passengers appear more and more
                    closeAllDoors();
                    Event departure = new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_DEPARTURED);
                    this.notifyAbout(departure);
                    currentStation.notifyAbout(departure);
                    currentStation.releaseBusPlace();
                } while(routeIterator.hasNext());
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
            } // for the entering process finished
        }
    }
    
    
    @Override
    public String toString() {
        return busId;
    }
    
}
