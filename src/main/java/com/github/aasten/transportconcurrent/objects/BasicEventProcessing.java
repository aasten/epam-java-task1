package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.human.Attention;

public class BasicEventProcessing implements Runnable, EventEnvironment {

    private final Set<Attention> allAttentions = new HashSet<>();
    private volatile Queue<Event> eventQueue = new ArrayDeque<Event>();
//    private volatile Object allAttentionsNotifiedAboutEvent = new Object();
//    private volatile Map<Event,Integer> currentlyNotifiedAttentionCount = new HashMap<>();
//    // prevent from invoking run() method from several threads
//    private final Object soleEventProcessing = new Object();
    
    @Override
    public void subscribeToEvents(Attention attention) {
        synchronized(allAttentions) {
//            synchronized(currentlyNotifiedAttentionCount) {
            allAttentions.add(attention);
                // add this attention to actual events
//                for(Event keyEvent : currentlyNotifiedAttentions.keySet()) {
//                    currentlyNotifiedAttentions.get(keyEvent).add(attention);
//                }
//                for(Entry<Event,Integer> entry : currentlyNotifiedAttentionCount.entrySet()) {
//                    entry.setValue(entry.getValue() + 1);
//                }
//            }
        }
    }

    @Override
    public void unSubscribe(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.remove(attention);
//            synchronized(currentlyNotifiedAttentionCount) {
//                // add this attention to actual events
////                for(Event keyEvent : currentlyNotifiedAttentions.keySet()) {
////                    currentlyNotifiedAttentions.get(keyEvent).remove(attention);
////                }
//                for(Entry<Event,Integer> entry : currentlyNotifiedAttentionCount.entrySet()) {
//                    entry.setValue(entry.getValue() - 1);
//                }
//            }
        }
    }

    @Override
    public void notifyAbout(Event event) {
//        synchronized (allAttentionsNotifiedAboutEvent) {
//            synchronized(allAttentions) {
//                synchronized(currentlyNotifiedAttentionCount) {
//                    currentlyNotifiedAttentionCount.put(event, allAttentions.size());
//                }
//            }
            synchronized(eventQueue) {
                eventQueue.add(event);
                // actually, notifyOne might be used for the one-threaded launchInfinitely() call
                // but keeping to be not dependent on this single-threading processing 
                eventQueue.notifyAll();
            }
//            try {
//                do {
//                    allAttentionsNotifiedAboutEvent.wait();
//                } while(anyAttentionNotNotifiedAbout(event));
//            } catch (InterruptedException e) {
//                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
//            }
//        }
    }
    
//    private boolean anyAttentionNotNotifiedAbout(Event event) {
//        synchronized (currentlyNotifiedAttentionCount) {
//            return currentlyNotifiedAttentionCount.containsKey(event);
//        }
//    }

    // to be run in a separate thread
    @Override
    public void run() {
//        synchronized (soleEventProcessing) {
        while(true) {
            Event currentEvent = takeEvent();
            // TODO optimize? (blocks allAttentions from insertions/deletions)
            synchronized(allAttentions) {
                for(Attention attention : allAttentions) {
                    attention.notifyAbout(currentEvent);
                }
            }
        }
//        }
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
    
//    @Override
//    public void eventWasNoticed(Event event) {
//        synchronized (allAttentionsNotifiedAboutEvent) {
//            synchronized (currentlyNotifiedAttentionCount) {
//                if(anyAttentionNotNotifiedAbout(event)) {
//                    int notNotifiedYet = currentlyNotifiedAttentionCount.get(event); 
//                    if(notNotifiedYet > 1) {
//                        currentlyNotifiedAttentionCount.put(event, notNotifiedYet - 1);                        
//                    } else {
//                        currentlyNotifiedAttentionCount.remove(event);
//                        allAttentionsNotifiedAboutEvent.notifyAll();
//                    }
//                } else {
//                    allAttentionsNotifiedAboutEvent.notifyAll();
//                    LoggerFactory.getLogger(getClass()).warn("Bug may be here " + getClass() + 
//                            "#eventWasNoticed(): received feedback of event \"" + event + 
//                            "\" that has no expectants");
//                }
//            }
//        }
//    }
}
