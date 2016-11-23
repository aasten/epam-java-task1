package com.github.aasten.transportconcurrent.objects;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.EventEnvironmentFeedback;
import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Behavior;


public class BasicEventProcessingWaitFeed implements Runnable, EventEnvironment, EventEnvironmentFeedback {

    private final Set<Attention> allAttentions = new HashSet<>();
    private volatile Queue<Entry<Event,String/*notification session ID*/>> eventQueue = 
            new ArrayDeque<>();
    private final Map<String/*session UID*/,Entry<Event,Map<Attention,String/*notificaion UID*/>>> 
        notificationSessions = new HashMap<>();
    // java.util.concurrent.ThreadLocalRandom not used here due to task restrictions
    private static final Random RANDOM = new Random();
    
    private static final class FeedingBackEventDecorator implements Event {

        private final Event originalEvent;
        private final String notificationUID;
        private final EventEnvironmentFeedback feedback;
        
        
        public FeedingBackEventDecorator(Event originalEvent, 
                String notificationUID, EventEnvironmentFeedback feedback) {
            this.originalEvent = originalEvent;
            this.notificationUID = notificationUID;
            this.feedback = feedback;
        }
        
        @Override
        public Date getTimestamp() {
            return originalEvent.getTimestamp();
        }

        @Override
        public void affectBehavior(Behavior behavior) {
            originalEvent.affectBehavior(behavior);
            feedback.eventWasNoticed(notificationUID);
        }
        
    }
    
    @Override
    public void subscribeToEvents(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.add(attention);
            // do not including into the current notification sessions
        }
    }

    @Override
    public void unSubscribe(Attention attention) {
        synchronized(allAttentions) {
            allAttentions.remove(attention);
            removeFromNotificationSessions(attention);
        }
    }
    
    private void removeFromNotificationSessions(Attention attention) {
        synchronized(notificationSessions) {
            for(Entry<String,Entry<Event,Map<Attention,String>>> e : notificationSessions.entrySet()) {
                e.getValue().getValue().remove(attention);
            }
            notificationSessions.notifyAll();
        }
    }

    private static String genUID() {
        return Double.toString(RANDOM.nextDouble());
    }

    @Override
    public void notifyAbout(Event event) {
        synchronized (notificationSessions) {
               
            Entry<Event,Map<Attention,String/*notification UID*/>> newSessionEntry = 
                    new AbstractMap.SimpleEntry<Event,Map<Attention,String>>(event, 
                            new HashMap<Attention,String>());
            synchronized (allAttentions) {
                for(Attention a : allAttentions) {
                    newSessionEntry.getValue().put(a, genUID());
                }
            }
            final String sessionID = genUID();
            notificationSessions.put(sessionID, newSessionEntry);
//        synchronized (allAttentionsNotifiedAboutEvent) {
//            synchronized(allAttentions) {
//                synchronized(currentlyNotifiedAttentionCount) {
//                    currentlyNotifiedAttentionCount.put(event, allAttentions.size());
//                }
//            }
            synchronized(eventQueue) {
                eventQueue.add(new AbstractMap.SimpleEntry<>(event, sessionID));
                // actually, notifyOne might be used for the one-threaded launchInfinitely() call
                // but keeping to be not dependent on this single-threading processing 
                eventQueue.notifyAll();
            }
            try {
                do {
                    notificationSessions.wait();
                } while(notificationSessions.containsKey(sessionID));
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(getClass()).warn(e.getMessage());
            }
        }
    }
    

    // to be run in a separate thread
    @Override
    public void run() {
        while(true) {
            Entry<Event,String> currentEntry = takeNextEntry(); 
            Event currentEvent = currentEntry.getKey();
            String sessionID = currentEntry.getValue();
            // TODO optimize? (blocks allAttentions from insertions/deletions)
            synchronized(allAttentions) {
                for(Attention attention : allAttentions) {
                    synchronized(notificationSessions) {
                        String notificationUID = notificationSessions.
                                get(sessionID).getValue().get(attention);
                        attention.notifyAbout(new FeedingBackEventDecorator(
                                currentEvent, notificationUID, this));
                    }
                }
            }
        }
    }
    
    
    private Entry<Event,String> takeNextEntry() {
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
    public void eventWasNoticed(String notificationUID) {
        synchronized(notificationSessions) {
            boolean found = false;
            for(Entry<String,Entry<Event,Map<Attention,String>>> e : notificationSessions.entrySet()) {
                for(Entry<Attention,String> e2 : e.getValue().getValue().entrySet()) {
                    if(e2.getValue().equals(notificationUID)) {
                        // remove this entry
                        e.getValue().getValue().remove(e2.getKey());
                        // ensuring in uniqueness of the notificationUID
                        found = true;
                        break;
                    }
                }
                if(found) {
                    if(e.getValue().getValue().isEmpty()) {
                        notificationSessions.remove(e.getKey());
                        notificationSessions.notifyAll();
                    }
                    break;
                }
            }
        }
    }    
}
