package tests;

import java.net.MalformedURLException;
import java.net.URL;

import com.y0ga.Networking.WebClient;

public class Shared {

    public static URL GetURLFromString(String str) {

        try {
            return new URL(str);
        } catch (MalformedURLException ignored) {return null;}

    }

    public static final WebClient client = new WebClient();

    public static final URL DUMMY_FILE_URL = GetURLFromString("http://www.ovh.net/files/1Mio.dat");

}
