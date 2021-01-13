package com.y0ga.Networking;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

class HttpIOStreamTunnel {

    private InputStream input;
    private OutputStream output;

    private HttpURLConnection underlyingConnection;

    public HttpIOStreamTunnel(InputStream input, OutputStream output) {

        this.input = input;
        this.output = output;

    }

    public HttpIOStreamTunnel(HttpURLConnection connection, InputStream input, OutputStream output) {

        this.underlyingConnection = connection;

        this.input = input;
        this.output = output;

    }

    public InputStream getUnderlyingInput() throws IOException {
        return underlyingConnection.getInputStream();
    }

    public OutputStream getUnderlyingOutput() throws IOException {
            return underlyingConnection.getOutputStream();
    }

    public HttpIOStreamTunnel copy(StreamCopySettings settings) throws IOException {

        long BufferCopyStartTime    = 0;
        long BufferCopyEndTime      = 0;

        int ToSleep                 = 0;

        int ReadOneSecond           = 0;

        long MaximumByteRate        = 0;

        boolean IsEOS               = false;

        do {

            ReadOneSecond = 0;

            BufferCopyStartTime = TimeUtility.get_ms();

            MaximumByteRate = settings.bps;

            if (settings.limitationMode == LimitationMode.Global) {

                int taskCount = settings.taskCounter.getRunningTaskCount();

                if (taskCount > 0) {

                    MaximumByteRate /= taskCount;

                }

            }

            while (ReadOneSecond < MaximumByteRate) {

                byte[] readBuffer = new byte[settings.bufferSize];

                int justRead = this.getInput().read(readBuffer, 0, readBuffer.length);

                if (justRead == StreamUtility.Constants.END_OF_STREAM) {

                    IsEOS = true;

                    break;

                }

                ReadOneSecond += justRead;

                this.getOutput().write(readBuffer, 0, justRead);

            }

            BufferCopyEndTime = TimeUtility.get_ms();

            ToSleep = (int) (TimeUtility.SECOND - (BufferCopyEndTime - BufferCopyStartTime));

            if ((ToSleep > 0) && (ToSleep <= TimeUtility.SECOND) && !IsEOS) {

                try {TimeUtility.SleepAccurate(ToSleep);}
                catch(InterruptedException ex){break;}

            }

        } while (!IsEOS);

        return this;
        
    }

    public InputStream getInput() {
            return input;
        }
    public OutputStream getOutput() {
        return output;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }
    public void setOutput(OutputStream output) {
        this.output = output;
    }


    public void close(boolean main, boolean underlying) {

        if (main) {
    
            try { this.input.close(); } catch (Exception ignored) {}
            try { this.output.close(); } catch (Exception ignored) {}
    
        }
        
        if (underlying) {

            try { getUnderlyingInput().close(); } catch (Exception ignored) {}
            try { getUnderlyingOutput().close(); } catch (Exception ignored) {}

        }

    }

}
