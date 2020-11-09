package tests;

import com.y0ga.Networking.WebClient;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class HeaderTests {

    private URL HEADER_ECHO = Shared.GetURLFromString("https://postman-echo.com/headers");

    @Test
    public void testHeaderEcho() throws IOException {

        WebClient client = new WebClient();

        client.Headers().clear();

        client.Headers().put("RandomHeaderKey", "RandomHeaderValue");

        String echo = client.downloadString(HEADER_ECHO);

        assert(echo.contains("\"randomheaderkey\":\"RandomHeaderValue\""));

    }

}