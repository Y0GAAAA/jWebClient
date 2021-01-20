package tests;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.y0ga.Networking.Asynchronous.AsyncTask;
import com.y0ga.Networking.WebClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Future;

public class UploadTests {

    private static final URL ECHO_URL = Shared.GetURLFromString("http://127.0.0.1/post_echo");

    private static final String TESTING_STRING = "This is an example string.";

    private static RawPostEchoServer EchoServer = null;

    @BeforeClass
    public static void startEchoServer() throws Exception {

        EchoServer = new RawPostEchoServer();

        EchoServer.start();

    }

    @AfterClass
    public static void stopEchoServer() throws Exception {

        EchoServer.stop();

    }

    @Test
    public void testUploadString() throws Exception {
        
        String echo = new WebClient().uploadString(ECHO_URL, TESTING_STRING);

        Assert.assertEquals(TESTING_STRING, echo);

    }

    @Test
    public void testUploadData() throws Exception {
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
    
        new WebClient().uploadData(ECHO_URL, new ByteArrayInputStream(TESTING_STRING.getBytes()), output);

        Assert.assertArrayEquals(TESTING_STRING.getBytes(), output.toByteArray());

    }

    @Test
    public void testUploadStringAsync() throws Exception {

        AsyncTask<String> echoFuture = new WebClient().uploadStringAsync(ECHO_URL, TESTING_STRING);

        String echo = echoFuture.await();

        Assert.assertEquals(TESTING_STRING, echo);

    }

    @Test
    public void testUploadDataAsync() throws Exception {

        byte[] data = TESTING_STRING.getBytes();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        AsyncTask dataFuture = new WebClient().uploadDataAsync(ECHO_URL, new ByteArrayInputStream(data), output);

        dataFuture.await();
        
        byte[] echo = output.toByteArray();

        Assert.assertArrayEquals(data, echo);

    }

    private static class RawPostEchoServer {

        HttpServer server;

        void start() throws Exception {

             this.server = HttpServer.create(new InetSocketAddress(80), 0);

            server.createContext("/post_echo", new PostHandler());
            server.start();

        }

        void stop() throws Exception {

            server.stop(0);

        }

        class PostHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange t) throws IOException {

                if (!t.getRequestMethod().equals("POST"))
                    t.close();

                t.sendResponseHeaders(200, 0);

                OutputStream output = t.getResponseBody();
                InputStream input = t.getRequestBody();

                byte[] buffer = new byte[8 * 1024]; //I will kill myself if I have to copy streams with a loop again in my entire life
                int len;
                while ((len = input.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }

                output.flush();

                t.close();

            }

        }

    }

}