package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent.EventType;
import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.objects.Route.RouteElement;

// TODO seems to have to many tasks
public class Bus implements EventEnvironment {

    private final int CAPACITY;
    private int currentPlacesTaken = 0;
    private final List<Doors> doors;
    private Station currentStation;
    private Route route;
    private volatile double averageSpeedMeterPerSec;
    private double initialDelay;
    private EventEnvironment delegateEventProcessing = new BasicEventProcessing();
    // waiting for queues populated after arriving before entering/exiting
    private final long WAIT_PASSENGERS_AT_DOORS_MSEC = 1000;
    private final Object passengerEntering = new Object();
//    private final Object allPassengersPassed = new Object();
    
    public Bus(int capacity, int doorsCount, Route route, double averSpeedMeterPerSec,
            double atFirstStationAfterSeconds) {
        this.CAPACITY = capacity;
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
            return currentPlacesTaken >= CAPACITY;
        }
    }
    
    boolean enter(Passenger passenger) {
        synchronized(passengerEntering) {
            if(!isFull()) {
                currentPlacesTaken++;
                subscribeToEvents(passenger.getAttention());
                // notify passenger through own event environment
                notifyAbout(new PassengerBusStationEvent(
                            passenger, this, currentStation, 
                            EventType.PASSENGER_ENTERED_BUS));
//                // TODO inefficient, replace with some event?
//                if(isAllDoorsQueuesEmpty()) {
//                    boardingFinished();
//                }
                return true;
            } else {
//                boardingFinished();
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
    
    // TODO optimize this by using events from doors instead of this method?
//    private boolean isAllDoorsQueuesEmpty() {
//        boolean thereIsSomebody = false; // consider empty apriori 
//        for(Doors d : doors) {
//            if(d.enterQueueLength() > 0 || d.exitQueueLength() > 0) {
//                thereIsSomebody |= true;
//            }
//        }
//        return !thereIsSomebody;
//    }
    
//    @Deprecated
//    private void boardingFinished() {
//        closeAllDoors();
//        synchronized(allPassengersPassed) {
//            allPassengersPassed.notifyAll(); // to continue walking the route
//        }
//    }
    
    void exit(Passenger passenger) {
        synchronized(passengerEntering) {
            if(currentPlacesTaken > 0 ) {
                currentPlacesTaken--;
            }
            unSubscribe(passenger.getAttention());
            currentStation.notifyAbout(new PassengerBusStationEvent(
                    passenger, this, currentStation, 
                    EventType.PASSENGER_EXITED_BUS));
//            // TODO inefficient, replace with some event? 
//            if(isAllDoorsQueuesEmpty()) {
//                boardingFinished();
//            }
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
        delegateEventProcessing.notifyAbout(event); 
    }

    public void launchInfinitely() {
        delegateEventProcessing.launchInfinitely();
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
//                    synchronized(allPassengersPassed) {
                    openAllDoors();
                    // wait for passengers to fill queues for exit and enter
                    Thread.sleep(WAIT_PASSENGERS_AT_DOORS_MSEC);
                    // will close after single iteration for processing queues
                    // which has been formed for this time
                    // TODO infinite cycle may be here if passengers appear more and more
                    closeAllDoors();
//                        allPassengersPassed.wait(); // for all passengers entered
//                    }
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
    
}
