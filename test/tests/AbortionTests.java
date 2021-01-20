package tests;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.y0ga.Networking.Asynchronous.AsyncTask;
import com.y0ga.Networking.WebClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AbortionTests {

    private static ErrorServer server = null;

    private static final URL FORBIDDEN_URL      = Shared.GetURLFromString("http://127.0.0.1/forbidden");
    private static final URL INTERRUPTED_URL    = Shared.GetURLFromString("http://127.0.0.1/interrupt");
    
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

        AsyncTask<String> futureString = new WebClient().downloadStringAsync(FORBIDDEN_URL);

        try {

            futureString.await();

        }
        catch (ExecutionException ignored) {return;}
        catch (InterruptedException ignored) {}

        assert(false);

    }
    
    @Test
    public void testForbiddenError() {
    
        try {
        
            new WebClient().downloadData(FORBIDDEN_URL, new AimlessOutputStream());
        
        } catch (IOException ignored) {return;}
    
        assert(false);
        
    }
    
    //@Test //for some reason can't detect unexpected stream closing yet...
    public void testAsyncInterruptedError() {
        
        AsyncTask<String> futureString = new WebClient().downloadStringAsync(INTERRUPTED_URL);
        
        try {
            
            futureString.await();
            
        }
        catch (Exception ignored) {return;}
        
        assert(false);
        
    }
    
    //@Test //for some reason can't detect unexpected stream closing yet...
    public void testInterruptedError() {
    
        try {
        
            new WebClient().downloadData(INTERRUPTED_URL, new AimlessOutputStream());
            
        }
        catch (IOException ignored) {return;}
    
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
