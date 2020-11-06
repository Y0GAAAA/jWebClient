package com.y0ga.Networking;

public class BandwidthLimitation {

    private long MaximumBytesSecond = 0;

    long getMaximumBytesSecond() {

        return this.MaximumBytesSecond;

    }

    public static final BandwidthLimitation Unlimited = new BandwidthLimitation(SizeUnit.TeraByte, Short.MAX_VALUE);

    public BandwidthLimitation(SizeUnit unit, int count) {

        if (count == 0) {

            this.MaximumBytesSecond = BandwidthLimitation.Unlimited.MaximumBytesSecond;

        } else {

            this.MaximumBytesSecond = (unit.getBytesCount() * count);

        }

    }

    public BandwidthLimitation(long maximumBytesSecond) {

        this.MaximumBytesSecond = maximumBytesSecond;

    }

    public BandwidthLimitation add(BandwidthLimitation limit) {

        BandwidthLimitation output = BandwidthLimitation.Unlimited;

        output.MaximumBytesSecond = this.MaximumBytesSecond += limit.MaximumBytesSecond;

        return output;

    }

    public BandwidthLimitation substract(BandwidthLimitation limit) {

        BandwidthLimitation output = BandwidthLimitation.Unlimited;

        output.MaximumBytesSecond = this.MaximumBytesSecond -= limit.MaximumBytesSecond;

        return output;

    }

}