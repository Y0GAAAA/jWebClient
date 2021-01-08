package com.y0ga.Networking;

import java.io.IOException;

class StreamUtility {

    private static class Constants {

        static final int END_OF_STREAM = -1;

    }

    public static void copyStream(long maximumBytesPerSecond, int bufferSize, ConcurrentTaskCounter taskCounter, LimitationMode limitationMode, HttpIOStreamTunnel tunnel) throws IOException {

        long BufferCopyStartTime    = 0;
        long BufferCopyEndTime      = 0;

        int ToSleep                 = 0;

        int ReadOneSecond           = 0;

        long MaximumByteRate        = 0;

        boolean IsEOS               = false;

        do {

            ReadOneSecond = 0;

            BufferCopyStartTime = TimeUtility.get_ms();

            MaximumByteRate = maximumBytesPerSecond;

            if (limitationMode == LimitationMode.Global) {

                int taskCount = taskCounter.getRunningTaskCount();

                if (taskCount > 0) {

                    MaximumByteRate /= taskCount;

                }

            }

            while (ReadOneSecond < MaximumByteRate) {

                byte[] readBuffer = new byte[bufferSize];

                int justRead = tunnel.getInput().read(readBuffer, 0, readBuffer.length);

                if (justRead == Constants.END_OF_STREAM) {

                    IsEOS = true;

                    break;

                }

                ReadOneSecond += justRead;

                tunnel.getOutput().write(readBuffer, 0, justRead);

            }

            BufferCopyEndTime = TimeUtility.get_ms();

            ToSleep = (int) (TimeUtility.SECOND - (BufferCopyEndTime - BufferCopyStartTime));

            if ((ToSleep > 0) && (ToSleep <= TimeUtility.SECOND) && !IsEOS) {

                try {TimeUtility.SleepAccurate(ToSleep);}
                catch(InterruptedException ex){return;}

            }

        } while (!IsEOS);

    }

}