package test;

import com.y0ga.Networking.WebClient;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ConcurrencyTests {

    @Test
    public void testIsBusyRaceCondition() throws Exception {

        Future<byte[]> f1 = Shared.client.downloadDataAsync(Shared.DUMMY_FILE_URL);

        boolean futureRunning = !f1.isDone();
        boolean webClientBusy = Shared.client.isBusy();

        Assert.assertEquals(futureRunning, webClientBusy);

    }

    @Test
    public void testIsBusyDoesNotImpactOtherInstances() throws Exception {

        WebClient client = new WebClient();

        client.downloadDataAsync(Shared.DUMMY_FILE_URL);

        WebClient secondClient = new WebClient();

        Assert.assertEquals(false, secondClient.isBusy());

    }

}
