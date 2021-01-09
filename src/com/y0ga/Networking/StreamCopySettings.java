package com.y0ga.Networking;

class StreamCopySettings {

    long bps; //max bytes per second
    int bufferSize; //copy buffer size

    ConcurrentTaskCounter taskCounter;
    LimitationMode limitationMode;

    public StreamCopySettings(long maxBps, int bufferSize, ConcurrentTaskCounter taskCounter, LimitationMode limitationMode) {

        this.bps = maxBps;
        this.bufferSize = bufferSize;
        this.taskCounter = taskCounter;
        this.limitationMode = limitationMode;

    }

}