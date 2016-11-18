package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public class TowardsBackwardsCyclicRoute implements Route {

    private ArrayList<Station> stationListTowards = new ArrayList<>();
    private LinkedHashMap<Station, HashMap<Station, Double>> routeMap;
    
    
    public TowardsBackwardsCyclicRoute(
            LinkedHashMap<Station,/*another station on the towards direction*/
                          HashMap<Station,/*neighbor station, towards or backwards*/
                                  Double/*distance*/>> towardsDirection) {
        routeMap = towardsDirection;
        stationListTowards.addAll(towardsDirection.keySet());        
        // completion the route map
        for(Station s : towardsDirection.keySet()) {
            for(Entry<Station, Double> entry : towardsDirection.get(s).entrySet()) {
                // backwards distance
                routeMap.get(entry.getKey()).put(s, entry.getValue());
            }
        }
    }
    
    public Iterator<RouteElement> getFirst() {
        return new Iterator<RouteElement>() {
            private boolean isEmpty = stationListTowards.size()<1;
            private boolean towards = true;
            private ListIterator<Station> iterator = stationListTowards.listIterator();
            private Station currentStation;
            
            public boolean hasNext() {
                return !isEmpty;
            }

            public RouteElement next() {
                if(isEmpty) throw new NoSuchElementException();
                if(towards) {
                    if(iterator.hasNext()) {
                        final Station station = iterator.next();
                        final double distance = (null == currentStation) ? 0
                                : routeMap.get(currentStation).get(station);
                        currentStation = station;
                        return new RouteElement() {
                            public Station nextStation() {
                                return station;
                            }
                            public double distanceMeters() {
                                return distance;
                            }
                        };
                    } else {
                        towards = false;
                        // "The first call to previous returns the same element as the last call to next."
                        // https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html
                        iterator.previous();
                        return next();
                    }
                } else { // backwards
                    if(iterator.hasPrevious()) {
                        final Station station = iterator.previous();
                        final double distance = (null == currentStation) ? 0
                                : routeMap.get(currentStation).get(station);
                        currentStation = station;
                        return new RouteElement() {
                            public Station nextStation() {
                                return station;
                            }
                            public double distanceMeters() {
                                return distance;
                            }
                        };
                    } else {
                        towards = true;
                        // "Similarly, the first call to next after a sequence of calls to previous 
                        // returns the same element as the last call to previous."
                        // https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html
                        iterator.next();  
                        return next();
                    }
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }
}
