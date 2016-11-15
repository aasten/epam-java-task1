package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent;
import com.github.aasten.transportconcurrent.events.PassengerBusStationEvent.EventType;
import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;

// TODO seems to have to many tasks
public class Bus implements EventEnvironment {

    private List<Attention> notifiable;
    private final int CAPACITY;
    private final List<Doors> doors;
    private final Queue<Event> eventQueue = new ArrayDeque<Event>();
    private Station currentStation;
    private Route route;
    
    public Bus(int capacity, int doorsCount, Route route) {
        this.CAPACITY = capacity;
        notifiable = new ArrayList<Attention>(capacity);
        if(doorsCount < 1) {
            // TODO log this
            doorsCount = 1;
        }
        doors = Collections.unmodifiableList(createDoorsList(doorsCount, this));
        currentStation = route.getFirst().next().nextStation;
    }
    
    private static List<Doors> createDoorsList(int doorsCount, Bus bus) {
        List<Doors> list = new ArrayList<Doors>(doorsCount);
        for(int i = 0; i < doorsCount; ++i) {
            list.add(new Doors(bus));
        }
        return list;
    }
    
    public boolean isFull() {
        synchronized(notifiable) {
            return notifiable.size() < CAPACITY;
        }
    }
    
    boolean enter(Passenger passenger) {
        synchronized(notifiable) {
            if(notifiable.size() < CAPACITY) {
                subscribeToEvents(passenger.getAttention());
                notifyAbout(new PassengerBusStationEvent(
                            passenger, this, currentStation, 
                            EventType.PASSENGER_ENTERED_BUS));
                return true;
            }
            return false;
        }
    }
    
    void exit(Passenger passenger) {
        synchronized(notifiable) {
            unSubscribe(passenger.getAttention());
        }
    }
    
    public List<Doors> getDoors() {
        return doors;
    }

    public void subscribeToEvents(Attention attention) {
        synchronized(notifiable) {
            notifiable.add(attention);
        }
    }

    public void unSubscribe(Attention attention) {
        synchronized(notifiable) {
            notifiable.remove(attention);
        }
    }

    public void notifyAbout(Event event) {
        synchronized(eventQueue) {
            eventQueue.add(event);
        }
        // actually, notifyOne might be used for the one-threaded launchInfinitely() call
        // but keeping to be not dependent on this single-threading processing 
        eventQueue.notifyAll(); 
    }

    public void launchInfinitely() {
        while(true) {
            Event currentEvent = takeEvent();
            for(Attention attention : notifiable) {
                attention.notifyAbout(currentEvent);
            }
        }
    }
    
    private Event takeEvent() {
        synchronized (eventQueue) {
            if(eventQueue.isEmpty()) {
                try {
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                }
            }
            // TODO optimizing extraction of subset of actually handled events 
            // may be here
            return eventQueue.poll();
        }
    }
    
    
}
