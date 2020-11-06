package com.y0ga.Networking;

public enum SizeUnit {

    KiloByte (10),
    MegaByte (20),
    GigaByte (30),
    TeraByte (40),
    ;

    private final double Power;
    private final static int PowerBase = 2;

    SizeUnit(double power) {

        this.Power = power;

    }

    long getBytesCount() {

        return (long) (Math.pow(PowerBase, this.Power));

    }

}