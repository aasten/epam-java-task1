package com.github.aasten.transportconcurrent.human;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;

public class QueuedAttention implements Attention {
    
    private int EVENT_QUEUE_MAX_SIZE = 1000;
    private Queue<Event> eventQueue = new ArrayDeque<Event>(EVENT_QUEUE_MAX_SIZE);

    public QueuedAttention() {
    }
    
    public void notifyAbout(Event event) {
        eventQueue.add(event);

    }

    public Iterator<Event> eventIterator() {
        return new Iterator<Event>(){

            private Iterator<Event> unsafe = eventQueue.iterator();
            
            public boolean hasNext() {
                synchronized(eventQueue) {
                    return unsafe.hasNext();
                }
            }

            public Event next() {
                synchronized(eventQueue) {
                    if(false == unsafe.hasNext()) {
                        try {
                            eventQueue.wait();
                        } catch (InterruptedException e) {
                            LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                        }
                    }
                    return unsafe.next();
                }
            }

            public void remove() {
                synchronized(eventQueue) {
                    unsafe.remove();
                }
            }
            
        };
    }

}
