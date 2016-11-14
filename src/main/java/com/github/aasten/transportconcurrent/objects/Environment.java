package com.github.aasten.transportconcurrent.objects;

import com.github.aasten.transportconcurrent.human.Attention;

public interface Environment {
    void subscribeToEvents(Attention attention);
    void unSubscribe(Attention attention);
}
