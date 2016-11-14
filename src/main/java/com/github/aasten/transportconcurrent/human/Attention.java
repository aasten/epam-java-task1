package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.events.BusStationEvent;

public interface Attention {
    void visit(BusStationEvent event);
}
