package test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Future;

public class AsyncAbortionTests {

    private static ErrorServer server = null;

    @BeforeClass
    public static void startErrorServer() throws Exception {

        server = new ErrorServer();

        server.start();

    }

    @AfterClass
    public static void stopErrorServer() throws Exception {

        server.stop();

    }

    @Test
    public void testAsyncForbiddenError() {

        Future<String> futureString = Shared.client.downloadStringAsync(Shared.GetURLFromString("http://127.0.0.1/forbidden"));

        try {

            futureString.get();

        } catch (Exception e) {return;}

        assert(false);

    }


    private static class ErrorServer {

        HttpServer server;

        void start() throws Exception {

            this.server = HttpServer.create(new InetSocketAddress(80), 0);

            server.createContext("/forbidden", new ForbiddenHandler());
            server.createContext("/interrupt", new InterruptExchangeHandler());
            server.start();

        }

        void stop() throws Exception {

            server.stop(0);

        }

        class ForbiddenHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange t) throws IOException {

                t.sendResponseHeaders(403, 0);

                t.close();

            }

        }

        class InterruptExchangeHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange t) throws IOException {

                int expectedLength = 50;

                t.sendResponseHeaders(200, expectedLength);

                OutputStream os = t.getResponseBody();

                for (int i = 0; i < expectedLength / 2; i++)
                    os.write(65);

                t.close(); //Close unexpectedly

            }

        }

    }

}
