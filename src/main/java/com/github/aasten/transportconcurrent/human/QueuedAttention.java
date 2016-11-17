package com.github.aasten.transportconcurrent.human;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;

public class QueuedAttention implements Attention, Iterator<Event> {
    
    private int EVENT_QUEUE_MAX_SIZE = 1000;
    private Queue<Event> eventQueue = new ArrayDeque<Event>(EVENT_QUEUE_MAX_SIZE);

    public QueuedAttention() {
    }
    
    public void notifyAbout(Event event) {
        synchronized(eventQueue) {
            eventQueue.add(event);
            eventQueue.notifyAll();
        }
    }

            
    public boolean hasNext() {
        synchronized(eventQueue) {
            return !eventQueue.isEmpty();
        }
    }

    public Event next() {
        synchronized(eventQueue) {
            if(false == hasNext()) {
                try {
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                }
            }
            return eventQueue.poll();
        }
    }


//            
//        };

//    }

}
