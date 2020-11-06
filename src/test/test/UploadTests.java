package test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Future;

public class UploadTests {

    private static final URL ECHO_URL = Shared.GetURLFromString("http://127.0.0.1/post_echo");

    private static final String TESTING_STRING = "This is an example string.";

    private static RawPostEchoServer EchoServer = null;

    @BeforeClass
    public static void startEchoServer() throws Exception {

        System.out.println("Starting echo server...");

        EchoServer = new RawPostEchoServer();

        EchoServer.start();

        System.out.println("Started echo server.");

    }

    @Test
    public void testUploadString() throws Exception {

        String echo = Shared.client.uploadString(ECHO_URL, TESTING_STRING);

        Assert.assertEquals(TESTING_STRING, echo);

    }

    @Test
    public void testUploadData() throws Exception {

        byte[] data = TESTING_STRING.getBytes();

        byte[] echo = Shared.client.uploadData(ECHO_URL, data);

        Assert.assertArrayEquals(data, echo);

    }

    @Test
    public void testUploadStringAsync() throws Exception {

        Future<String> echoFuture = Shared.client.uploadStringAsync(ECHO_URL, TESTING_STRING);

        String echo = echoFuture.get();

        Assert.assertEquals(TESTING_STRING, echo);

    }

    @Test
    public void testUploadDataAsync() throws Exception {

        byte[] data = TESTING_STRING.getBytes();

        Future<byte[]> dataFuture = Shared.client.uploadDataAsync(ECHO_URL, data);

        byte[] echo = dataFuture.get();

        Assert.assertArrayEquals(data, echo);

    }

    private static class RawPostEchoServer {

        HttpServer server;

        void start() throws Exception {

             this.server = HttpServer.create(new InetSocketAddress(80), 0);

            server.createContext("/post_echo", new PostHandler());
            server.start();

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