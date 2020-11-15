package com.y0ga.Networking;

import java.util.concurrent.atomic.AtomicInteger;

class ConcurrentTaskCounter {

    private AtomicInteger RunningTaskCount = new AtomicInteger(0);

    public int getRunningTaskCount() {
        return RunningTaskCount.get();
    }

    public void incrementRunningTaskCount() {
        RunningTaskCount.incrementAndGet();
    }

    public void decrementRunningTaskCount() {
        RunningTaskCount.decrementAndGet();
    }

}