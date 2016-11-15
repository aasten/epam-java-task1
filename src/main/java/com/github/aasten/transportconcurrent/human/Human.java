package com.github.aasten.transportconcurrent.human;

public abstract class Human {
    public abstract Attention getAttention();
    abstract void setBehavior(Behavior behavior);
    public abstract Behavior getBehavior();
//    Role getRole();
}
