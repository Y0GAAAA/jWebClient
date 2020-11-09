package tests;

import com.y0ga.Networking.BandwidthLimitation;
import com.y0ga.Networking.LimitationMode;
import com.y0ga.Networking.SizeUnit;
import com.y0ga.Networking.WebClient;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.y0ga.Networking.Utils.TimeUtility.get_ms;

public class SpeedLimitTests {

    private WebClient client = new WebClient();

    private int GetExecutionTime(Callable r) {

        long start = get_ms();

        try {

            r.call();

        } catch (Exception ignored) {}

        long end = get_ms();

        return (int)(end - start);

    }

    @Test
    public void testSimpleLimit() throws Exception {

        client.setDownloadBandwidthLimit(new BandwidthLimitation(SizeUnit.KiloByte, 100));

        int elapsed = GetExecutionTime(() -> client.downloadData(Shared.DUMMY_FILE_URL));

        System.out.println("Elapsed : " + elapsed + " ms");

        if (elapsed < 1000 * 10) { //if it took less than 10 seconds at the rate of 100kb/s for 1Mb
            assert(false); //Shit happened
        }

    }

    @Test
    public void testPerTaskLimit() throws Exception {

        client.setLimitationMode(LimitationMode.PerTask);

        client.setDownloadBandwidthLimit(new BandwidthLimitation(SizeUnit.KiloByte, 50));

        int elapsed = GetExecutionTime(() -> {

            client.downloadDataAsync(Shared.DUMMY_FILE_URL);
            client.downloadDataAsync(Shared.DUMMY_FILE_URL);

            while (client.isBusy())
                Thread.sleep(10);

            return null;

        });

        if (elapsed < 1000 * 18) { //if it took less than ~20 (18 because it can "overshoot") seconds at the rate of 50kb/s for 1Mb
            assert(false); //Shit happened
        }

    }

}
