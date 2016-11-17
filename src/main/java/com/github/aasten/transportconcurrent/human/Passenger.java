package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.system.HavingTarget;

public class Passenger extends Human implements HavingTarget {

    private QueuedAttention attention = new QueuedAttention();
    private Behavior currentBehavior;
    private Station destination;
    private boolean targetIsAchieved = false;
    
    public Passenger(Station initial, Station destination) {
        this.destination = destination;
        currentBehavior = new PassengerBehaviorAtStation(this, this.destination);
        initial.subscribeToEvents(attention);
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
    
    public Station getDestination() {
        return destination;
    }
    
    public void targetAchieving() {
        while(false == targetIsAchieved) {
            attention.next().affectBehavior(getBehavior());
        }
    }

}
