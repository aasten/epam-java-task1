package com.github.aasten.transportconcurrent.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
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
        // TODO verifying and completion the route map
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
