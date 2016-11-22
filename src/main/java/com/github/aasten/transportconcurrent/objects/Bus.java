package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.BusStationEvent;
import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.EventEnvironmentFeedback;
import com.github.aasten.transportconcurrent.events.EventWithFeedback;
import com.github.aasten.transportconcurrent.events.IncomingEventsProcessing;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent.EventType;
import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.objects.Route.RouteElement;

// TODO seems to have to many tasks
public class Bus implements EventEnvironment, EventEnvironmentFeedback, IncomingEventsProcessing {

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
    private final Object passengerInOut = new Object();
    private final String busId;
    private final Set<Passenger> passengersInside = new HashSet<>();
    private final Object allAttentionsNotified = new Object();
    private final Map<Event,Set<Object>> unnotified = new HashMap<>();
    
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
        synchronized(passengerInOut) {
            return currentPlacesTaken >= capacity;
        }
    }
    
    boolean enter(Passenger passenger) {
        synchronized(passengerInOut) {
            if(!isFull()) {
                currentPlacesTaken++;
                passengersInside.add(passenger);
                // notifying the passenger directly
                // do not waiting for his reaction
                passenger.getAttention().notifyAbout(new PassengerBusStationEvent(
                        passenger, this, currentStation, 
                        EventType.PASSENGER_ENTERED_BUS));
//                currentStation.notifyAbout(new PassengerBusStationEvent(
//                        passenger, this, currentStation, 
//                        EventType.PASSENGER_ENTERED_BUS, currentStation));
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
        synchronized(passengerInOut) {
            if(currentPlacesTaken > 0 ) {
                currentPlacesTaken--;
            }
            passengersInside.remove(passenger);
            // notifying the passenger directly
            // do not waiting for his reaction
            passenger.getAttention().notifyAbout(new PassengerBusStationEvent(
                    passenger, this, currentStation, 
                    EventType.PASSENGER_EXITED_BUS));
//            notifyAbout(new PassengerBusStationEvent(
//                    passenger, this, currentStation, 
//                    EventType.PASSENGER_EXITED_BUS, this));
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
    
    private void notifyPassengersInsideAboutAndWaitFeedback(EventWithFeedback event) {
        synchronized(allAttentionsNotified) {
        
            if(!unnotified.containsKey(event)) {
                unnotified.put(event, new HashSet<>(passengersInside.size()));
            }
            unnotified.get(event).addAll(passengersInside);
            delegateEventProcessing.notifyAbout(event);
            do {
                try {
                    allAttentionsNotified.wait();
                } catch (InterruptedException e) {
                    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                }
            } while(!unnotified.get(event).isEmpty());
        }
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
                    notifyPassengersInsideAboutAndWaitFeedback(new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_ARRIVED,
                            this));
//                    this.notifyAbout(new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_ARRIVED,
//                            this));
                    currentStation.notifyAbout(new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_ARRIVED,
                            currentStation));
                    openAllDoors();
                    // wait for passengers to fill queues for exit and enter
                    Thread.sleep(WAIT_PASSENGERS_AT_DOORS_MSEC);
                    // will close after single iteration for processing queues
                    // which has been formed for this time
                    // TODO infinite cycle may be here if passengers appear more and more
                    closeAllDoors();

                    notifyPassengersInsideAboutAndWaitFeedback(new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_DEPARTURED,
                            this));
//                    this.notifyAbout(new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_DEPARTURED,
//                            this));

                    currentStation.notifyAbout(new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_DEPARTURED,
                            currentStation));

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

    @Override
    public void eventWasNoticedBy(Event event, Object objectNoticed) {
        synchronized(allAttentionsNotified) {
            unnotified.get(event).remove(objectNoticed);
            allAttentionsNotified.notifyAll();
        }
    }
    
}
