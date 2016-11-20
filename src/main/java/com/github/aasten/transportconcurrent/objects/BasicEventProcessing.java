package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.EventEnvironmentFeedback;
import com.github.aasten.transportconcurrent.human.Attention;

public class BasicEventProcessing implements Runnable, EventEnvironmentFeedback {

    Set<Attention> allAttentions = new HashSet<>();
    private final Queue<Event> eventQueue = new ArrayDeque<Event>();
    private final Object allAttentionsNotifiedAboutEvent = new Object();
    private final Map<Event,Set<Attention>> currentlyNotifiedAttentions = new HashMap<>();
    // prevent from invoking run() method from several threads
    private final Object soleEventProcessing = new Object();
    
    @Override
    public void subscribeToEvents(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.add(attention);
            synchronized(currentlyNotifiedAttentions) {
                // add this attention to actual events
//                for(Event keyEvent : currentlyNotifiedAttentions.keySet()) {
//                    currentlyNotifiedAttentions.get(keyEvent).add(attention);
//                }
                for(Entry<Event,Set<Attention>> entry : currentlyNotifiedAttentions.entrySet()) {
                    entry.getValue().add(attention);
                }
            }
        }
    }

    @Override
    public void unSubscribe(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.remove(attention);
            synchronized(currentlyNotifiedAttentions) {
                // add this attention to actual events
//                for(Event keyEvent : currentlyNotifiedAttentions.keySet()) {
//                    currentlyNotifiedAttentions.get(keyEvent).remove(attention);
//                }
                for(Entry<Event,Set<Attention>> entry : currentlyNotifiedAttentions.entrySet()) {
                    entry.getValue().remove(attention);
                }
            }
        }
    }

    @Override
    public void notifyAbout(Event event) {
        synchronized(eventQueue) {
            synchronized(currentlyNotifiedAttentions) {
                synchronized(allAttentions) {
                    currentlyNotifiedAttentions.put(event, new HashSet<>(allAttentions));
                }
            }
            eventQueue.add(event);
            // actually, notifyOne might be used for the one-threaded launchInfinitely() call
            // but keeping to be not dependent on this single-threading processing 
            eventQueue.notifyAll();
        }
        synchronized (allAttentionsNotifiedAboutEvent) {
            try {
                do {
                    allAttentionsNotifiedAboutEvent.wait();
                } while(anyAttentionNotNotifiedAbout(event));
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
            }
        }
    }
    
    private boolean anyAttentionNotNotifiedAbout(Event event) {
        synchronized (currentlyNotifiedAttentions) {
            return !currentlyNotifiedAttentions.get(event).isEmpty();
        }
    }

    // to be run in a separate thread
    @Override
    public void run() {
        synchronized (soleEventProcessing) {
            while(true) {
                Event currentEvent = takeEvent();
                // TODO optimize? (blocks allAttentions from insertions/deletions)
                synchronized(allAttentions) {
                    for(Attention attention : allAttentions) {
                        attention.notifyAbout(currentEvent);
                        attentionWasNotified(attention, currentEvent);
                    }
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
    
    @Override
    public void attentionWasNotified(Attention attention, Event event) {
        synchronized (allAttentionsNotifiedAboutEvent) {
            synchronized (currentlyNotifiedAttentions) {
                currentlyNotifiedAttentions.get(event).remove(attention);
                if(currentlyNotifiedAttentions.get(event).isEmpty()) {
                    allAttentionsNotifiedAboutEvent.notifyAll();
                }
            }
        }
    }
}
