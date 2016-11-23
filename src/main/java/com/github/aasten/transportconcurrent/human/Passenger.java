package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.system.HavingTarget;

public class Passenger extends Human implements HavingTarget {

    private QueuedAttention attention = new QueuedAttention();
    private Behavior currentBehavior;
    private Station destination;
    private boolean targetIsAchieved = false;
    private final String id;
    
    public Passenger(String id, Station initial, Station destination) {
        this.id = id;
        this.destination = destination;
        currentBehavior = new PassengerBehaviorAtStation(this, initial);
    }
    
    public Attention getAttention() {
        return attention;
    }
    
    @Override
    void setBehavior(Behavior behavior) {
        synchronized (this) {
            currentBehavior = behavior;
        }
    }
    
    @Override
    public Behavior getBehavior() {
        synchronized (this) {
            return currentBehavior;
        }
    }


    public boolean isTargetAchieved() {
        return targetIsAchieved;
    }

    void targetIsAchieved() {
        targetIsAchieved = true;
    }
    
    public Station getDestination() {
        return destination;
    }
    
    public void targetAchieving() {
        while(false == targetIsAchieved) {
            attention.next().affectBehavior(getBehavior());
        }
    }
    
    @Override
    public String toString() {
        return "P" + id + "(--> " + destination + ")"; 
    }

}
