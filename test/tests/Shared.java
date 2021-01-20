package tests;

import java.net.MalformedURLException;
import java.net.URL;

public class Shared {

    public static URL GetURLFromString(String str) {

        try {
            return new URL(str);
        } catch (MalformedURLException ignored) {return null;}

    }
    
    /**
     * URL pointing to a 1MB file
     */
    public static final URL SMALL_FILE_URL = GetURLFromString("http://www.ovh.net/files/1Mio.dat");
    
    /**
     * URL pointing to a 10GB file.
     */
    public static final URL HUGE_FILE_URL = GetURLFromString("http://www.ovh.net/files/10Gio.dat");

}
