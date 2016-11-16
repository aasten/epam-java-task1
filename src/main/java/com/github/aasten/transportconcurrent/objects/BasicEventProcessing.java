package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Attention;

public class BasicEventProcessing implements EventEnvironment {

    List<Attention> allAttentions = new ArrayList<>();
    private final Queue<Event> eventQueue = new ArrayDeque<Event>();
    
    @Override
    public void subscribeToEvents(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.add(attention);
        }

    }

    @Override
    public void unSubscribe(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.remove(attention);
        }
    }

    @Override
    public void notifyAbout(Event event) {
        synchronized(eventQueue) {
            eventQueue.add(event);
        }
        // actually, notifyOne might be used for the one-threaded launchInfinitely() call
        // but keeping to be not dependent on this single-threading processing 
        eventQueue.notifyAll(); 
    }

    @Override
    public void launchInfinitely() {
        while(true) {
            Event currentEvent = takeEvent();
            // TODO optimize? (blocks allAttentions from insertions/deletions)
            synchronized(allAttentions) {
                for(Attention attention : allAttentions) {
                    attention.notifyAbout(currentEvent);
                }
            }
        }

    }
    
    private Event takeEvent() {
        synchronized (eventQueue) {
            if(eventQueue.isEmpty()) {
                try {
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
                }
            }
            // TODO optimizing extraction of subset of actually handled events 
            // may be here
            return eventQueue.poll();
        }
    }
    
    List<Attention> getAllAttentions() {
        synchronized(allAttentions) {
            return Collections.unmodifiableList(allAttentions);
        }
        
    }

}
