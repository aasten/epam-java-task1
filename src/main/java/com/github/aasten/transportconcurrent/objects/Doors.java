package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.human.Passenger;

// TODO get rid of synchronized() overhead
public class Doors {
    
    private static class DoorState {
        private boolean isOpen = false;
        DoorState(boolean isOpen) {
            this.isOpen = isOpen;
        }
        void setIsOpen(boolean newState) {
            isOpen = newState;
        }
        boolean isOpen() { return isOpen; }
    }
    
    private Bus bus;
//    private Boolean doorIsOpen = new Boolean(false);
    private final DoorState doorState = new DoorState(false);
    private Queue<Passenger> enterQueue = new ArrayDeque<Passenger>();
    private Queue<Passenger> exitQueue = new ArrayDeque<Passenger>();
    
    public Doors(Bus bus) {
        this.bus = bus;
    }
    
    void open() {
        synchronized(doorState) {
            doorState.setIsOpen(true);
            doorState.notifyAll();
        }
    }
    
    void close() {
        synchronized(doorState) {
            doorState.setIsOpen(false);
            synchronized(exitQueue) {
                exitQueue.clear();
            }
            synchronized(enterQueue) {
                enterQueue.clear();
            }
        }
    }
    
    public void process() {
        while(true) {
            try {
                synchronized(doorState) {
                    if(false == doorState.isOpen() ||
                            (enterQueueLength() < 1 && exitQueueLength() < 1)) {
                        doorState.wait(); // for open
                    }
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
            } 
            synchronized(exitQueue) {
                while(!exitQueue.isEmpty() && doorState.isOpen()) {
                    exit(exitQueue.poll());
                }
            }
            synchronized(enterQueue) {
                while((!enterQueue.isEmpty() || !bus.isFull())  && doorState.isOpen()) {
                    tryEnter(enterQueue.poll());
                }
            }
        }
    }
    
    /**
     * 
     * @return result of entering
     * @retval false if doors are closed 
     */
    void tryEnter(Passenger passenger) {
        synchronized(doorState) {
            if(doorState.isOpen()) {
                bus.enter(passenger);
            }
        }
    }
    void exit(Passenger passenger) {
        synchronized(doorState) {
            if(doorState.isOpen()) {
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
