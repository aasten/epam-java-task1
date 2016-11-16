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
    private Integer currentPlacesTaken = 0;
    private final List<Doors> doors;
    private Station currentStation;
    private Route route;
    private double averageSpeedMeterPerSec;
    private double initialDelay;
    private EventEnvironment delegateEventProcessing = new BasicEventProcessing();
    
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
    
    private static List<Doors> createDoorsList(int doorsCount, Bus bus) {
        List<Doors> list = new ArrayList<Doors>(doorsCount);
        for(int i = 0; i < doorsCount; ++i) {
            list.add(new Doors(bus));
        }
        return list;
    }
    
    public boolean isFull() {
        synchronized(currentPlacesTaken) {
            return currentPlacesTaken < CAPACITY;
        }
    }
    
    boolean enter(Passenger passenger) {
        synchronized(currentPlacesTaken) {
            if(!isFull()) {
                currentPlacesTaken++;
                subscribeToEvents(passenger.getAttention());
                // notify passenger through own event environment
                notifyAbout(new PassengerBusStationEvent(
                            passenger, this, currentStation, 
                            EventType.PASSENGER_ENTERED_BUS));
                if(isAllDoorsQueuesEmpty()) {
                    boardingFinished();
                }
                return true;
            } else {
                boardingFinished();
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
    private boolean isAllDoorsQueuesEmpty() {
        boolean thereIsSomebody = false; // consider empty apriori 
        for(Doors d : doors) {
            if(d.enterQueueLength() > 0 || d.exitQueueLength() > 0) {
                thereIsSomebody |= true;
            }
        }
        return !thereIsSomebody;
    }
    
    private void boardingFinished() {
        closeAllDoors();
        Event departure = new BusStationEvent(this, currentStation, 
                BusStationEvent.EventType.BUS_DEPARTURED);
        currentStation.notifyAbout(departure);
        this.notifyAbout(departure);
        synchronized(this) {
            notifyAll(); // to continue walking the route
        }
    }
    
    void exit(Passenger passenger) {
        synchronized(currentPlacesTaken) {
            if(currentPlacesTaken > 0 ) {
                currentPlacesTaken--;
            }
            unSubscribe(passenger.getAttention());
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
        try {
            Thread.sleep((long)(1000*initialDelay));
            while(routeIterator.hasNext()) {
                final RouteElement r = routeIterator.next();
                currentStation = r.nextStation();
                openAllDoors();
                Event arriving = new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_ARRIVED);
                this.notifyAbout(arriving);
                currentStation.notifyAbout(arriving);
                synchronized(this) {
                    wait(); // for all passengers entered
                }
                Event departure = new BusStationEvent(this,currentStation,BusStationEvent.EventType.BUS_DEPARTURED);
                this.notifyAbout(departure);
                currentStation.notifyAbout(departure);
                Thread.sleep((long)(1000*r.distanceMeters()/averageSpeedMeterPerSec));
            }
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).warn(e.getMessage());
        } // for the entering process finished
    }
    
}
