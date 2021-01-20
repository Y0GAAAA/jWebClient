package tests;

import com.y0ga.Networking.Asynchronous.AsyncTask;
import com.y0ga.Networking.WebClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Future;

public class ConcurrencyTests {

    @Test
    public void testIsBusyRaceCondition() throws Exception {
        
        WebClient client = new WebClient();
        
        client.downloadDataAsync(Shared.SMALL_FILE_URL, new AimlessOutputStream());
        
        Assert.assertEquals(client.isBusy(), true);

    }

    @Test
    public void testTaskCountIsAccurate() throws Exception {

        WebClient client = new WebClient();

        assert(client.getRunningTaskCount() == 0);

        client.downloadDataAsync(Shared.HUGE_FILE_URL, new AimlessOutputStream());

        assert(client.getRunningTaskCount() == 1);

        client.downloadDataAsync(Shared.HUGE_FILE_URL, new AimlessOutputStream());

        assert(client.getRunningTaskCount() == 2);

        client.downloadData(Shared.SMALL_FILE_URL, new AimlessOutputStream());

        assert(client.getRunningTaskCount() == 2);

    }

}
