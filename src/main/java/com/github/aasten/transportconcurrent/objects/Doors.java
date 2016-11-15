package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.Queue;

import com.github.aasten.transportconcurrent.human.Passenger;

public class Doors {
    
    private Bus bus;
    private boolean opened;
    private Queue<Passenger> enterQueue = new ArrayDeque<Passenger>();
    private Queue<Passenger> exitQueue = new ArrayDeque<Passenger>();
    
    public Doors(Bus bus) {
        this.bus = bus;
    }
    
    void openDoor() {
        synchronized(this) {
            opened = true;
            notifyAll();
        }
    }
    
    void closeDoor() {
        synchronized(this) {
            opened = false;
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public void process() {
        synchronized(exitQueue) {
            while(!exitQueue.isEmpty()) {
                exit(exitQueue.poll());
            }
        }
        synchronized(enterQueue) {
            while(!enterQueue.isEmpty() || !bus.isFull()) {
                tryEnter(enterQueue.poll());
            }
        }
    }
    
    /**
     * 
     * @return result of entering
     * @retval false if doors are closed 
     */
    boolean tryEnter(Passenger passenger) {
        synchronized(this) {
            if(opened) {
                return bus.enter(passenger);
            }
            return false;
        }
    }
    void exit(Passenger passenger) {
        synchronized(this) {
            if(opened) {
                bus.exit(passenger);
            }
            // else TODO assert, runtime exception, etc 
        }
    }
    
    public void enqueueEnter(Passenger passenger) {
        synchronized(enterQueue) {
            enterQueue.add(passenger);
        }
    }
    public void enqueueExit(Passenger passenger) {
        synchronized(exitQueue) {
            exitQueue.add(passenger);
        }
    }
    
    public int enterQueueLength() {
        synchronized(enterQueue) {
            return enterQueue.size();
        } 
    }
    
    public int exitQueueLength() {
        synchronized(exitQueue) {
            return exitQueue.size();
        } 
    }
    
    
}
