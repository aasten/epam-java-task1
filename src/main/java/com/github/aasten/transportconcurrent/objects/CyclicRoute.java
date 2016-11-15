package com.github.aasten.transportconcurrent.objects;

import java.util.Iterator;
import java.util.Set;

public class CyclicRoute implements Route {

    
    private Set<Station> stationSet;
    
    public CyclicRoute(Set<Station> stationSet) {
        this.stationSet = stationSet;
    }
    
    public Iterator<RouteElement> getFirst() {
        return new Iterator<RouteElement>() {

            public boolean hasNext() {
                // route is cyclic
                return true;
            }

            public RouteElement next() {
                // TODO Auto-generated method stub
                return null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }
}
