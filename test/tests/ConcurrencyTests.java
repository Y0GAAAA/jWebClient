package tests;

import com.y0ga.Networking.WebClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Future;

public class ConcurrencyTests {

    @Test
    public void testIsBusyRaceCondition() throws Exception {

        Future<byte[]> f1 = Shared.client.downloadDataAsync(Shared.SMALL_FILE_URL);

        boolean futureRunning = !f1.isDone();
        boolean webClientBusy = Shared.client.isBusy();

        Assert.assertEquals(futureRunning, webClientBusy);

    }

    @Test
    public void testIsBusyDoesNotImpactOtherInstances() throws Exception {

        WebClient client = new WebClient();

        client.downloadDataAsync(Shared.SMALL_FILE_URL);

        WebClient secondClient = new WebClient();

        Assert.assertEquals(false, secondClient.isBusy());

    }

    @Test
    public void testTaskCountIsAccurate() throws Exception {

        WebClient client = new WebClient();

        assert(client.getRunningTaskCount() == 0);

        client.downloadDataAsync(Shared.HUGE_FILE_URL);

        assert(client.getRunningTaskCount() == 1);

        client.downloadDataAsync(Shared.HUGE_FILE_URL);

        assert(client.getRunningTaskCount() == 2);

        client.downloadData(Shared.SMALL_FILE_URL);

        assert(client.getRunningTaskCount() == 2);

    }

}
