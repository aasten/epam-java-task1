package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;

public class Bus implements Environment {

    private List<Attention> notifiable;
    private final int CAPACITY;
    private final List<Doors> doors;
    
    public Bus(int capacity, int doorsCount) {
        this.CAPACITY = capacity;
        notifiable = new ArrayList<Attention>(capacity);
        if(doorsCount < 1) {
            // TODO log this
            doorsCount = 1;
        }
        doors = Collections.unmodifiableList(createDoorsList(doorsCount, this));
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
                notifiable.add(passenger.getAttention());
                return true;
            }
            return false;
        }
    }
    
    void exit(Passenger passenger) {
        synchronized(notifiable) {
            notifiable.remove(passenger.getAttention());
        }
    }
    
    public List<Doors> getDoors() {
        return doors;
    }

    public void subscribeToEvents(Attention attention) {
        
    }

    public void unSubscribe(Attention attention) {
        // TODO Auto-generated method stub
        
    }
    
    
}
