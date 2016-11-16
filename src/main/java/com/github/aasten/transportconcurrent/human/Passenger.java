package com.github.aasten.transportconcurrent.human;

import java.util.Iterator;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.system.HavingTarget;

public class Passenger extends Human implements HavingTarget {

    private HumanAttention attention = new HumanAttention(this);
    private Behavior currentBehavior;
    private Station destination;
    private boolean targetIsAchieved = false;
    
    public Passenger(Station destination) {
        this.destination = destination;
        currentBehavior = new PassengerBehaviorAtStation(this, this.destination);
    }
    
    public Attention getAttention() {
        return attention;
    }
    
    @Override
    void setBehavior(Behavior behavior) {
        synchronized (currentBehavior) {
            currentBehavior = behavior;
        }
    }
    
    @Override
    public Behavior getBehavior() {
        synchronized (currentBehavior) {
            return currentBehavior;
        }
    }

//    public Role getRole() {
        // TODO Auto-generated method stub
//        return role;
//    }

    public boolean isTargetAchieved() {
        return targetIsAchieved;
    }

    void targetIsAchieved() {
        targetIsAchieved = true;
    }
    
    Station getDestination() {
        return destination;
    }
    
    void targetAchieving() {
        Iterator<Event> eventIterator = attention.eventIterator();
        // TODO check need of synchronization
        while(false == targetIsAchieved) {
            eventIterator.next().affectBehavior(getBehavior());
        }
    }

}
