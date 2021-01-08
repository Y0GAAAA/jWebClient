package com.y0ga.Networking;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class HttpIOStreamTunnel implements Closeable {

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

    @Override
    public void close() {

        try {

            this.input.close();

        } catch (IOException ignored){}

        try {

            this.output.close();

        } catch (IOException ignored){}

    }

}
