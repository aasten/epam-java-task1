package com.github.aasten.transportconcurrent.objects;

import java.util.Iterator;

public interface Route {
    interface RouteElement {
        Station nextStation();
        double distanceMeters();
    }
    Iterator<RouteElement> getFirst();
}
