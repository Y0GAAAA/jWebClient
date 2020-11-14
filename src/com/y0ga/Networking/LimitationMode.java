package com.y0ga.Networking;

/**
 * Enum defining how the bandwidth is shared between tasks.
 */
public enum LimitationMode {

    /**
     * Equally splits the bandwidth limit between all running tasks.
     */
    Global,

    /**
     * Each task can use the set bandwidth limit. Using this mode : two simultaneous downloads limited at 500Kbps will use a total of 1Mbps.
     */
    PerTask,

}
