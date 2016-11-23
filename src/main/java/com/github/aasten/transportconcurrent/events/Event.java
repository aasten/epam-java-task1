package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Behavior;

public interface Event {
    void affectBehavior(Behavior behavior);
}
