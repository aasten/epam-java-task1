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
    
    private final long WAIT_PASSENGER_AFTER_QUEUE_EMPTY_MSEC = 1000;
    
    private Bus bus;
    private final DoorState doorState = new DoorState(false);
    private final Queue<Passenger> enterQueue = new ArrayDeque<Passenger>();
    private final Queue<Passenger> exitQueue = new ArrayDeque<Passenger>();
    private final Object passingThrough = new Object();
    
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
        synchronized(passingThrough) {
            try {
                passingThrough.wait();
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
            }
            synchronized(doorState) {
                doorState.setIsOpen(false);
            }
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
            synchronized(passingThrough) {
                // process all the queues that are filled for this moment
                try {
                    synchronized(doorState) {
                            
                        if(false == doorState.isOpen()) {
                            doorState.wait(); // for open
                        }
                    }
                 
                    synchronized(exitQueue) {
                        while(!exitQueue.isEmpty()) {
                            exit(exitQueue.remove());
                            if(exitQueue.isEmpty()) {
                             // wait for passenger if is not enqueued yet
                                exitQueue.wait(WAIT_PASSENGER_AFTER_QUEUE_EMPTY_MSEC);
                            }
                        }
                    }
                    synchronized(enterQueue) {
                        while((!enterQueue.isEmpty() && !bus.isFull())) {
                            tryEnter(enterQueue.remove());
                            if(enterQueue.isEmpty() && !bus.isFull()) {
                                // wait for passenger if is not enqueued yet
                                enterQueue.wait(WAIT_PASSENGER_AFTER_QUEUE_EMPTY_MSEC);
                            }
                        }
                        if(!enterQueue.isEmpty()) {
                            // bus is full
                            enterQueue.clear();
                        }
                    }
                        
                        
                } catch (InterruptedException e) {
                    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                }
                passingThrough.notifyAll();
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
            enterQueue.notifyAll();
        }
    }
    public void enqueueExit(Passenger passenger) {
        synchronized(exitQueue) {
            exitQueue.add(passenger);
            exitQueue.notifyAll();
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
