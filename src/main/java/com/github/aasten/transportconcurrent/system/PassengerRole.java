package com.github.aasten.transportconcurrent.system;

import com.github.aasten.transportconcurrent.human.Behavior;

@Deprecated
public class PassengerRole implements Role {

    private Behavior currentBehavior;
    
    public void setBehavior(Behavior behavior) {
        currentBehavior = behavior;
    }

    public Behavior getCurrentBehavior() {
        return currentBehavior;
    }

}
