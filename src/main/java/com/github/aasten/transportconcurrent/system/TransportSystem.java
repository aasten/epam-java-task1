package com.github.aasten.transportconcurrent.system;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.events.Event;
import com.github.aasten.transportconcurrent.events.IncomingEventsProcessing;
import com.github.aasten.transportconcurrent.human.Attention;
import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Doors;
import com.github.aasten.transportconcurrent.objects.EventEnvironment;
import com.github.aasten.transportconcurrent.objects.Route;
import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.objects.TowardsBackwardsCyclicRoute;

public class TransportSystem {
    
    private static class ResourceConstants {
        public enum APP_MODE {
            CLI,
            GUI, // unsupported for now
        }
        public enum NAME {
            APP_MODE,
            PASSENGERS_TOTAL_COUNT,
            STATION_COUNT_TOWARDS,
            BUS_COUNT,
            EACH_BUS_CAPACITY,
            DOORS_COUNT_EACH_BUS,
            BUS_ROUTE_INITIAL_TIME_INTERVAL_SEC,
            BUS_AVERAGE_SPEED_METER_PER_SEC,
            MIN_DISTANCE_BETWEEN_STATIONS_METERS,
            MAX_DISTANCE_BETWEEN_STATIONS_METERS,
        }
    }
    
    // for logging completion before shutting down its daemon thread
    private static final long SLEEP_PASSENGER_AFTER_TARGET_ACHIEVED = 1000;
    
    
    public static void execute(ResourceBundle resourceBundle) throws IllegalArgumentException {
        // TODO read property file
        // creating in threads: passengers, buses, stations 
        // launch threads and join them
        String appMode = resourceBundle.getString(ResourceConstants.NAME.APP_MODE.toString().toLowerCase());
        if(false == appMode.toUpperCase().equals(ResourceConstants.APP_MODE.CLI.toString()))
        {
            LoggerFactory.getLogger(TransportSystem.class).error("Unsupported app mode: {}, quitting", appMode);
            throw new IllegalArgumentException("Not supported app mode for now: " + appMode);
        }
        
        LinkedHashMap<Station,HashMap<Station,Integer>> towardsRouteDirection = new LinkedHashMap<>();
        final List<Station> stationList = new ArrayList<>();
        
        {
            int stationCountTowards = intPropertyValue(ResourceConstants.NAME.STATION_COUNT_TOWARDS, resourceBundle);
            int minDistance = intPropertyValue(ResourceConstants.NAME.MIN_DISTANCE_BETWEEN_STATIONS_METERS, resourceBundle);
            int maxDistance = intPropertyValue(ResourceConstants.NAME.MAX_DISTANCE_BETWEEN_STATIONS_METERS, resourceBundle);
            if(stationCountTowards < 2) {
                throw new IllegalArgumentException("Station count must be greater than one, got " + 
                            stationCountTowards);
            }
            if(minDistance <= 0 || maxDistance <= 0) {
                throw new IllegalArgumentException("Both min and max distances between stations must be positive integers");
            }
            if(minDistance > maxDistance) {
                throw new IllegalArgumentException("Min distance should not be greater than max one");
            }
            for(int i = 0; i < stationCountTowards; i++) {
                stationList.add(new Station("S"+(i+1)));
                towardsRouteDirection.put(stationList.get(i), new HashMap<Station,Integer>());
                if(i > 0) {
                    setDistanceFromTo(towardsRouteDirection, stationList.get(i-1), stationList.get(i), 
                            (int) (minDistance + Math.random()*(maxDistance - minDistance)));
                }
            }
        } 
        
        
        final Route towardsBackwardsCyclicRoute = new TowardsBackwardsCyclicRoute(towardsRouteDirection);
        

        // TODO ThreadGroup?
        List<Thread> threads = new ArrayList<Thread>();

        
        List<Runnable> stationProcessings = new ArrayList<>();
        // infinitely launch event processing on event environment
        for(final IncomingEventsProcessing p : stationList) {
            stationProcessings.add(p.getEventProcessor());
        }
        addRunnablesToNewDaemonThreads(threads, stationProcessings);
        
        List<Passenger> passengers = new ArrayList<>();
        List<Runnable> passengerProcesses = new ArrayList<>();
        {
            int passengersCount = intPropertyValue(ResourceConstants.NAME.PASSENGERS_TOTAL_COUNT, resourceBundle);
            if(passengersCount < 1) {
                throw new IllegalArgumentException("Passengers count must be not less than one, got " + 
                        passengersCount);
            }
            for(int i = 0; i < passengersCount; i++) {
                int stationIndexFrom = (int) (Math.random() * stationList.size());
                int stationIndexTo = (int) (Math.random() * stationList.size());
                if(stationIndexTo == stationIndexFrom) {
                    // moving to nearest available area
                    if(stationList.size() - stationIndexFrom > 1) {
                        stationIndexTo++;
                    } else {
                        stationIndexTo--;
                    }
                    
                }
                final Passenger p = new Passenger(Integer.toString(i),
                                                  stationList.get(stationIndexFrom),
                                                  stationList.get(stationIndexTo));
                passengers.add(p);
                passengerProcesses.add(new Runnable() {
                    public void run() {
                        p.targetAchieving();
                        try {
                            Thread.sleep(SLEEP_PASSENGER_AFTER_TARGET_ACHIEVED);
                        } catch (InterruptedException e) {
                            LoggerFactory.getLogger(TransportSystem.class).warn(e.getMessage());
                        }
                    }
                });
            }
        } 
        
        
        addRunnablesToNewThreads(threads, passengerProcesses);
        
        LoggingEnvironment loggingEnvironment = new LoggingEnvironment();
        addRunnablesToNewDaemonThreads(threads, Collections.singletonList(loggingEnvironment));
        
        
        List<Bus> buses = new ArrayList<>();
        
        {
        int busCount = intPropertyValue(ResourceConstants.NAME.BUS_COUNT, resourceBundle);
        int busCapacity = intPropertyValue(ResourceConstants.NAME.EACH_BUS_CAPACITY, resourceBundle);
        int doorsCount = intPropertyValue(ResourceConstants.NAME.DOORS_COUNT_EACH_BUS, resourceBundle);
        int busInterval = intPropertyValue(ResourceConstants.NAME.BUS_ROUTE_INITIAL_TIME_INTERVAL_SEC, resourceBundle);
        int busSpeed = intPropertyValue(ResourceConstants.NAME.BUS_AVERAGE_SPEED_METER_PER_SEC, resourceBundle);
        
        for(int i = 0; i < busCount; i++) {
            buses.add(new Bus("B"+(i+1),busCapacity,doorsCount,towardsBackwardsCyclicRoute,busSpeed,busInterval*(i+1),loggingEnvironment));
        }

        }
        
        
        // busThread
        List<Runnable> busEventProcessings = new ArrayList<>();
        // infinitely launch event processing on event environment
        for(final IncomingEventsProcessing p : buses) {
            busEventProcessings.add(p.getEventProcessor());
        }
        addRunnablesToNewDaemonThreads(threads, busEventProcessings);
        
        List<Runnable> busRouteWalkings = new ArrayList<>();
        for(final Bus bus : buses) {
            busRouteWalkings.add(new Runnable() {
                public void run() {
                    bus.walkingTheRoute();
                }
            });
        }
        addRunnablesToNewDaemonThreads(threads, busRouteWalkings);
        
        
        // doorsThread
        List<Runnable> doorsProcesses = new ArrayList<>();
        for(Bus bus : buses) {
            for(final Doors d : bus.getDoors()) {
                doorsProcesses.add(new Runnable() {
                    @Override
                    public void run() {
                        d.process();
                    }
                });
            }
        }
        addRunnablesToNewDaemonThreads(threads, doorsProcesses);
        
        LoggerFactory.getLogger(TransportSystem.class).info("LOGGING LEGEND:" +
                "S means \"Station\", B means \"Bus\", P means \"Passenger\". \n" + 
                "LOGGING LEGEND: Line format: (<Event environment kind>=[Inside bus|At station]): " +
                "[<Event time>] <Event content with any use of 'P', 'S', 'B'>\n" + 
                "LOGGING LEGEND: Example: (At station): [Sun Nov 20 01:59:35 MSK 2016] S7 << B2\n" +
                "LOGGING LEGEND: Explanation: Event inside bus at Nov 20 01:59:35, Bus number 2 " + 
                "has arrived to the Station number 7\n" + 
                "LOGGING LEGEND: Example: (Inside bus): [Sun Nov 20 01:56:40 MSK 2016] S9:B7 >> P(--> S9)\n" +
                "LOGGING LEGEND: Explanation: Event inside the bus at Nov 20 01:56:40, Passenger whose destination is Station number 9 " +
                "has exited at Station number 9 from the Bus number 7");
        
        for(Thread t : threads) {
            t.start();
        }
        LoggerFactory.getLogger(TransportSystem.class).trace("Initialized and started");
        
    }
    
    
    private static int intPropertyValue(ResourceConstants.NAME forName, ResourceBundle bundle) throws IllegalArgumentException {
        String paramName = forName.toString().toLowerCase();
        try {
        String paramValue = bundle.getString(paramName);
        try {
            return Integer.parseInt(paramValue);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Bad " + paramName + " value: " + paramValue);
        }
        } catch(MissingResourceException e) {
            throw new IllegalArgumentException("Property " + paramName + " not defined");
        }
    }
    
    private static void setDistanceFromTo(
            LinkedHashMap<Station,HashMap<Station,Integer>> routeMap,
            Station from, Station to, int distance) {
        routeMap.get(from).put(to,distance);
    }
    
    private static void addRunnablesToNewThreads(List<Thread> threads, List<? extends Runnable> runnables) {
        for(Runnable r : runnables) {
            threads.add(new Thread(r));
        }
    }
    private static void addRunnablesToNewDaemonThreads(List<Thread> threads, List<? extends Runnable> runnables) {
        for(Runnable r : runnables) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            threads.add(t);
        }
    }
    
    private static class LoggingEnvironment implements EventEnvironment, Runnable {
        private Queue<Event> eventQueue = new ArrayDeque<>();

        @Override
        public void run() {
            while(true) {
                synchronized (eventQueue) {
                    if(eventQueue.isEmpty()) {
                        try {
                            eventQueue.wait();
                        } catch (InterruptedException e) {
                            LoggerFactory.getLogger(TransportSystem.class).warn(e.getMessage());
                        }
                    }
                    LoggerFactory.getLogger(TransportSystem.class).info(eventQueue.poll().toString());
                }
            }
        }

        @Override
        public void subscribeToEvents(Attention attention) {
            // do nothing
        }

        @Override
        public void unSubscribe(Attention attention) {
            // do nothing
        }

        @Override
        public void notifyAbout(Event event) {
            synchronized (eventQueue) {
                eventQueue.add(event);
                eventQueue.notifyAll();
            }
        }
        
        
    }
    
}
