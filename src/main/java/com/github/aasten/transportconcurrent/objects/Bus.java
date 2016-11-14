package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.List;

import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;

public class Bus implements Environment {

    private List<Attention> notifiable;
    private final int CAPACITY;
    
    public Bus(int capacity) {
        this.CAPACITY = capacity;
        notifiable = new ArrayList<Attention>(capacity);
    }
    
    public boolean isFull() {
        synchronized(notifiable) {
            return notifiable.size() < CAPACITY;
        }
    }
    
    public boolean enter(Passenger passenger) {
        synchronized(notifiable) {
            if(notifiable.size() < CAPACITY) {
                notifiable.add(passenger.getAttention());
                return true;
            }
            return false;
        }
    }
    
    public void exit(Passenger passenger) {
        synchronized(notifiable) {
            notifiable.remove(passenger.getAttention());
        }
    }

    public void subscribeToEvents(Attention attention) {
        
    }

    public void unSubscribe(Attention attention) {
        // TODO Auto-generated method stub
        
    }
    
    
}
