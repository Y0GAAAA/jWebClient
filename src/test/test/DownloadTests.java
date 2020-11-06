package test;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.Future;

public class DownloadTests {

    private static final URL EXAMPLE_WEBSITE = Shared.GetURLFromString("http://example.com/");

    private static final String EXAMPLE_WEBSITE_HTML = //region Html

            "<!doctype html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Example Domain</title>\n" +
                    "\n" +
                    "    <meta charset=\"utf-8\" />\n" +
                    "    <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                    "    <style type=\"text/css\">\n" +
                    "    body {\n" +
                    "        background-color: #f0f0f2;\n" +
                    "        margin: 0;\n" +
                    "        padding: 0;\n" +
                    "        font-family: -apple-system, system-ui, BlinkMacSystemFont, \"Segoe UI\", \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;\n" +
                    "        \n" +
                    "    }\n" +
                    "    div {\n" +
                    "        width: 600px;\n" +
                    "        margin: 5em auto;\n" +
                    "        padding: 2em;\n" +
                    "        background-color: #fdfdff;\n" +
                    "        border-radius: 0.5em;\n" +
                    "        box-shadow: 2px 3px 7px 2px rgba(0,0,0,0.02);\n" +
                    "    }\n" +
                    "    a:link, a:visited {\n" +
                    "        color: #38488f;\n" +
                    "        text-decoration: none;\n" +
                    "    }\n" +
                    "    @media (max-width: 700px) {\n" +
                    "        div {\n" +
                    "            margin: 0 auto;\n" +
                    "            width: auto;\n" +
                    "        }\n" +
                    "    }\n" +
                    "    </style>    \n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "<div>\n" +
                    "    <h1>Example Domain</h1>\n" +
                    "    <p>This domain is for use in illustrative examples in documents. You may use this\n" +
                    "    domain in literature without prior coordination or asking for permission.</p>\n" +
                    "    <p><a href=\"https://www.iana.org/domains/example\">More information...</a></p>\n" +
                    "</div>\n" +
                    "</body>\n" +
                    "</html>\n";
            //endregion

    private static byte[] ExampleWebsiteHtmlBytes() {
        return EXAMPLE_WEBSITE_HTML.getBytes();
    }

    @Test
    public void downloadStringTest() throws Exception {

        String htmlContent = Shared.client.downloadString(EXAMPLE_WEBSITE);

        Assert.assertEquals(EXAMPLE_WEBSITE_HTML, htmlContent);

    }

    @Test
    public void downloadDataTest() throws Exception {

        byte[] data = Shared.client.downloadData(EXAMPLE_WEBSITE);

        Assert.assertArrayEquals(ExampleWebsiteHtmlBytes(), data);

    }

    @Test
    public void downloadFileTest() throws Exception {

        File filePath = new File("test_example.html");

        filePath.deleteOnExit();

        boolean result = Shared.client.downloadFile(EXAMPLE_WEBSITE, filePath);

        Assert.assertTrue(result);

        byte[] fileBytes = Files.readAllBytes(filePath.toPath());

        String fileContent = new String(fileBytes);

        Assert.assertEquals(EXAMPLE_WEBSITE_HTML, fileContent);

    }

    @Test
    public void downloadStringAsyncTest() throws Exception {

        Future<String> contentFuture = Shared.client.downloadStringAsync(EXAMPLE_WEBSITE);

        String content = contentFuture.get();

        Assert.assertEquals(EXAMPLE_WEBSITE_HTML, content);

    }

    @Test
    public void downloadDataAsyncTest() throws Exception {

        Future<byte[]> futureData = Shared.client.downloadDataAsync(EXAMPLE_WEBSITE);

        byte[] data = futureData.get();

        Assert.assertArrayEquals(ExampleWebsiteHtmlBytes(), data);

    }

    @Test
    public void downloadFileAsyncTest() throws Exception {

        File filePath = new File("test_example_async.html");

        filePath.deleteOnExit();

        Future<Boolean> result = Shared.client.downloadFileAsync(EXAMPLE_WEBSITE, filePath);

        boolean success = result.get();

        Assert.assertTrue(success);

        byte[] fileBytes = Files.readAllBytes(filePath.toPath());

        Assert.assertArrayEquals(ExampleWebsiteHtmlBytes(), fileBytes);

    }

}
