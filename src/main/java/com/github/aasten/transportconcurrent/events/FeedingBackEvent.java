package com.github.aasten.transportconcurrent.events;

import com.github.aasten.transportconcurrent.human.Behavior;

@Deprecated
public abstract class FeedingBackEvent extends TimestampEvent implements EventWithFeedback {

    @Override
    public final void affectBehavior(Behavior behavior) {
        affectBehaviorBeforeFeedback(behavior);
//        getEnvironmentFeedback().eventWasNoticed(this);
    }

    public abstract void affectBehaviorBeforeFeedback(Behavior behavior);
}
