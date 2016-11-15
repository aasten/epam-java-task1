package com.github.aasten.transportconcurrent.objects;

import java.util.Iterator;

public interface Route {
    public static class RouteElement {
        Station nextStation;
        double distanceMeters;
    }
    Iterator<RouteElement> getFirst();
}
