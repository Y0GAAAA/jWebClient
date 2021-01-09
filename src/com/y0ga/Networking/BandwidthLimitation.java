package com.y0ga.Networking;

/**
 * Represents a maximum speed in units per second.
 */
public class BandwidthLimitation {

    private long MaximumBytesSecond = 0;

    long getMaximumBytesSecond() {

        return this.MaximumBytesSecond;

    }
    
    /**
     * Constant equivalent to no bandwidth limitation.
     */
    public static final BandwidthLimitation UNLIMITED = new BandwidthLimitation(SizeUnit.GigaByte, Short.MAX_VALUE);

    /**
     * Creates a new bandwidth limitation from the specified count of size unit.
     */
    public BandwidthLimitation(SizeUnit unit, short count) {

        if (count == 0) {

            this.MaximumBytesSecond = BandwidthLimitation.UNLIMITED.MaximumBytesSecond;

        } else {

            this.MaximumBytesSecond = (unit.getBytesCount() * count);

        }

    }

    /**
     * Creates a new bandwidth limitation from a maximum bytes per second count.
     */
    public BandwidthLimitation(long maximumBytesSecond) {

        this.MaximumBytesSecond = maximumBytesSecond;

    }

    /**
     * Adds two limitations and returns the result.
     */
    public BandwidthLimitation add(BandwidthLimitation other) {

        return new BandwidthLimitation(this.MaximumBytesSecond + other.MaximumBytesSecond);

    }

    /**
     * Subtracts two limitations and returns the result.
     */
    public BandwidthLimitation substract(BandwidthLimitation other) {

        return new BandwidthLimitation(this.MaximumBytesSecond - other.MaximumBytesSecond);

    }

}