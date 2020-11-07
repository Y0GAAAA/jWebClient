package com.y0ga.Networking.Utils;

import com.y0ga.Networking.Enums.LimitationMode;
import com.y0ga.Networking.Exceptions.ConnectionException;
import com.y0ga.Networking.TaskInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtility {

    private static class Constants {

        static final int END_OF_STREAM = -1;

    }

    public static void copyStream(long maximumBytesPerSecond, int bufferSize, LimitationMode limitationMode, InputStream input, OutputStream output) throws ConnectionException {

        long BufferCopyStartTime    = 0;
        long BufferCopyEndTime      = 0;

        long OperationTime          = 0;

        long ToSleep                = 0;

        int ReadOneSecond           = 0;

        long MaximumByteRate        = 0;

        boolean IsEOS               = false;

        do {

            ReadOneSecond = 0;

            BufferCopyStartTime = TimeUtility.get_ms();

            if (limitationMode == LimitationMode.Global) {

                int taskCount = TaskInfo.getRunningTaskCount();

                MaximumByteRate = maximumBytesPerSecond;

                if (taskCount > 0) {

                    MaximumByteRate /= taskCount;

                }

            }

            while (ReadOneSecond < MaximumByteRate) {

                byte[] readBuffer = new byte[bufferSize];

                try {

                    int justRead = input.read(readBuffer, 0, readBuffer.length);

                    if (justRead == Constants.END_OF_STREAM) {

                        IsEOS = true;

                        break;

                    }

                    ReadOneSecond += justRead;

                    output.write(readBuffer, 0, justRead);

                } catch (IOException ex) {

                    try {

                        input.close();
                        output.close();

                    } catch (IOException ignored){}

                    throw new ConnectionException("Underlying connection/streams were unexpectedly closed");

                }

            }

            BufferCopyEndTime = TimeUtility.get_ms();

            OperationTime = (BufferCopyEndTime - BufferCopyStartTime);

            ToSleep = TimeUtility.SECOND - OperationTime;

            if ((ToSleep > 0) && (ToSleep <= TimeUtility.SECOND) && !IsEOS) {

                try {Thread.sleep(ToSleep);}
                catch(InterruptedException ex){return;}

            }

        } while (!IsEOS);

    }

    public static void closeStreamTunnel(InputStream iStream, OutputStream oStream) {

        try {

            iStream.close();

        } catch (IOException ignored){}

        try {

            oStream.close();

        } catch (IOException ignored){}

    }

}
