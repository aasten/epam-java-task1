package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Passenger;

public interface PassengerEvent extends Event {
    Passenger getPassenger();
}
