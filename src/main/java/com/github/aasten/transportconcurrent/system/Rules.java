package com.github.aasten.transportconcurrent.system;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Passenger;

@Deprecated
public class Rules {
    public static Behaviour<Passenger> passengerBehaviourOutside() {
        return new Behaviour<Passenger>() {

            public void takeControlOver(Passenger managed) {
                // TODO Auto-generated method stub
                
            }

            public void behaveAccording(Event event, Passenger managed) {
                // TODO Auto-generated method stub
                
            }
            
        }
    }
}
