package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.human.Passenger;

// TODO get rid of synchronized() overhead
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
                wait(); // for open
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
            } 
            synchronized(exitQueue) {
                while(!exitQueue.isEmpty() && opened) {
                    exit(exitQueue.poll());
                }
            }
            synchronized(enterQueue) {
                while((!enterQueue.isEmpty() || !bus.isFull())  && opened) {
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
        synchronized(this) {
            if(opened) {
                bus.enter(passenger);
            }
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
