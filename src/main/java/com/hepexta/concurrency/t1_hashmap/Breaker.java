package com.hepexta.concurrency.t1_hashmap;

public class Breaker {
    volatile boolean shouldWork = true;

    public boolean isShouldWork() {
        return shouldWork;
    }

    public void setShouldWork(boolean shouldWork) {
        this.shouldWork = shouldWork;
    }
}
