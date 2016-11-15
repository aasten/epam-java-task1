package com.github.aasten.transportconcurrent.human;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import com.github.aasten.transportconcurrent.events.Event;

public class HumanAttention implements Attention {
    
    private Human human;
    private int EVENT_QUEUE_MAX_SIZE = 1000;
    private Queue<Event> eventQueue = new ArrayDeque<Event>(EVENT_QUEUE_MAX_SIZE);

    public HumanAttention(Human human) {
        this.human = human;
    }
    
    public void notifyAbout(Event event) {
        eventQueue.add(event);

    }

    public Iterator<Event> eventIterator() {
        // TODO Auto-generated method stub
        return null;
    }

}
