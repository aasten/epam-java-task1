package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.objects.Bus;

public interface BusEvent extends Event {
    Bus getBus();
}
