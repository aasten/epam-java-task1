package com.github.aasten.transportconcurrent.system;

import com.github.aasten.transportconcurrent.human.Behavior;

@Deprecated
public interface Role {
    void setBehavior(Behavior behavior);
    Behavior getCurrentBehavior();
}