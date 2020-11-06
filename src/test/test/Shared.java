package test;

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

}
