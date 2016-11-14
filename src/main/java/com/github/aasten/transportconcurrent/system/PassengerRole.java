package com.github.aasten.transportconcurrent.system;

import com.github.aasten.transportconcurrent.human.Passenger;

public class PassengerRole implements Role<Passenger> {

    private Behaviour<Passenger> currentBehaviour;
    
    public void setBehaviour(Behaviour<Passenger> behaviour) {
        currentBehaviour = behaviour;
    }

    public Behaviour<Passenger> getCurrentBehaviour() {
        return currentBehaviour;
    }

}
