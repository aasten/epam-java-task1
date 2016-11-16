package com.github.aasten.transportconcurrent.human;

// abstract to make setBehaviour() method package-private
public abstract class Human {
    public abstract Attention getAttention();
    abstract void setBehavior(Behavior behavior);
    public abstract Behavior getBehavior();
//    Role getRole();
}
