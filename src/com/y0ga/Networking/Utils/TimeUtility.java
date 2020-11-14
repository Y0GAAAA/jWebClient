package com.y0ga.Networking.Utils;

public class TimeUtility {

    static final int SECOND = 1000;
    private static final int SLEEPING_IMPRECISION_MS = 15;

    public static long get_ms() {

        return System.currentTimeMillis();

    }

    public static void SleepAccurate(int milliseconds) throws InterruptedException {

        final long spinUntil = get_ms() + milliseconds;
        final int rawSleep = milliseconds - SLEEPING_IMPRECISION_MS;

        if (rawSleep > SLEEPING_IMPRECISION_MS)
            Thread.sleep(rawSleep);

        while (get_ms() < spinUntil) {}

    }

}
