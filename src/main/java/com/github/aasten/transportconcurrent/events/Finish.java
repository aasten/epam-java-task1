package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Behavior;

public class Finish extends TimestampEvent implements Event {

    @Override
    public void affectBehavior(Behavior behavior) {
        behavior.behaveAccording(this);
    }

}
