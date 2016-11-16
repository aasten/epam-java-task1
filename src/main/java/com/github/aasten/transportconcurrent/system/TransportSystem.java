package com.github.aasten.transportconcurrent.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.github.aasten.transportconcurrent.human.Passenger;
import com.github.aasten.transportconcurrent.objects.Bus;
import com.github.aasten.transportconcurrent.objects.Doors;
import com.github.aasten.transportconcurrent.objects.Route;
import com.github.aasten.transportconcurrent.objects.Station;
import com.github.aasten.transportconcurrent.objects.TowardsBackwardsCyclicRoute;

public class TransportSystem {
    
    private static void setDistanceFromTo(
            LinkedHashMap<Station,HashMap<Station,Double>> routeMap,
            Station from, Station to, double distance) {
        routeMap.get(from).put(to,distance);
    }
    
    private static void addRunnablesToNewThreads(List<Thread> threads, List<Runnable> runnables) {
        for(Runnable r : runnables) {
            threads.add(new Thread(r));
        }
    }
    private static void addRunnablesToNewDaemonThreads(List<Thread> threads, List<Runnable> runnables) {
        for(Runnable r : runnables) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            threads.add(t);
        }
    }
    
    public static void execute(File propetyFile) throws IllegalArgumentException {
        // TODO read property file
        // creating in threads: passengers, buses, stations 
        // launch threads and join them     
        
        LinkedHashMap<Station,HashMap<Station,Double>> towardsRouteDirection = new LinkedHashMap<>();
        final List<Station> stationList = Arrays.asList(new Station("A"), new Station("B"));
        for(Station s : stationList) {
            towardsRouteDirection.put(s, new HashMap<Station,Double>());            
        }
        
        setDistanceFromTo(towardsRouteDirection, stationList.get(0), stationList.get(1), 100);
        
        
        final Route towardsBackwardsCyclicRoute = new TowardsBackwardsCyclicRoute(towardsRouteDirection);
        
        List<Runnable> passengerProcesses = new ArrayList<Runnable>();
        passengerProcesses.add(new Runnable() {
            public void run() {
                Passenger passenger = new Passenger(stationList.get(0),stationList.get(1));
                passenger.targetAchieving();
            }
        });
        // TODO ThreadGroup?
        List<Thread> threads = new ArrayList<Thread>();
        addRunnablesToNewThreads(threads, passengerProcesses);
        
        // busThread
        List<Bus> buses = new ArrayList<>();
        buses.add(new Bus(10,1,towardsBackwardsCyclicRoute,10,5));
        List<Runnable> busProcesses = new ArrayList<>();
        for(final Bus bus : buses) {
            busProcesses.add(new Runnable() {
                public void run() {
                    bus.launchInfinitely();
                }
            });
        }
        addRunnablesToNewDaemonThreads(threads, busProcesses);
        
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
        
        for(Thread t : threads) {
            t.start();
        }
        LoggerFactory.getLogger(TransportSystem.class).trace("Started execution");
        
        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(t.getClass()).warn(e.getMessage());
            }
        }
        
        LoggerFactory.getLogger(TransportSystem.class).trace("Finished execution");
        
    }
}
