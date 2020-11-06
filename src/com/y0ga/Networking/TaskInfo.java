package com.y0ga.Networking;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskInfo {

    private static AtomicInteger RunningTaskCount = new AtomicInteger(0);

    public static int getRunningTaskCount() {
        return RunningTaskCount.get();
    }

    public static void incrementRunningTaskCount() {
        RunningTaskCount.incrementAndGet();
    }

    public static void decrementRunningTaskCount() {
        RunningTaskCount.decrementAndGet();
    }

}
