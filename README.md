## You can find the full JavaDoc [here](https://y0gaaaa.github.io/jWebClient/com/y0ga/Networking/package-summary.html).

Asynchronous operations are handled by [Futures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html), if you're not familiar with this class, you should take a look at the linked documentation.
  
### If you're in a hurry, here is an example which exposes the primitive capacities :

```java
WebClient client = new WebClient();

URL URL = new URL("http://info.cern.ch/hypertext/WWW/TheProject.html");

String downloadedString = client.downloadString(URL);
byte[] downloadedData   = client.downloadData(URL);

client.downloadFile(URL, new File("D:\\TheProject.html"));

Future<String> futureString = client.downloadStringAsync(URL);
Future<byte[]> futureData   = client.downloadDataAsync(URL);

Future<Boolean> futureDownloadFileFlag = client.downloadFileAsync(URL, new File("D:\\TheProject.html"));

while (client.isBusy()) { //Must be busy because we started three asynchronous operations

    System.out.println("Busy...");

    Thread.sleep(500);

}
```
