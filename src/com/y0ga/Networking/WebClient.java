package com.y0ga.Networking;

import com.y0ga.Networking.Enums.*;
import com.y0ga.Networking.Exceptions.*;
import com.y0ga.Networking.Utils.BufferUtility;
import com.y0ga.Networking.Utils.StreamUtility;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.concurrent.Executors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The client object that can perform http requests.
 */
public class WebClient {

    //region PRIVATE

    private final ExecutorService ThreadPool = Executors.newCachedThreadPool();
    private final ConcurrentTaskCounter TaskCounter = new ConcurrentTaskCounter();

    private HttpURLConnection getConnection(URL url, HttpMethod method, RequestSpecification specification) throws IOException {

        HttpURLConnection remoteHostConnection = null;

        remoteHostConnection = (HttpURLConnection) url.openConnection();

        remoteHostConnection.setRequestMethod(method.getMethodString());

        remoteHostConnection.setDoOutput(method.getDoOutput());
        remoteHostConnection.setDoInput(true);

        remoteHostConnection.setRequestProperty("User-Agent", CurrentUserAgentString);

        specification.SetHeaders(remoteHostConnection);

        for (String headerKey : this.internalHeaders.keySet()) {

            String headerValue = this.internalHeaders.get(headerKey);

            remoteHostConnection.setRequestProperty(headerKey, headerValue);

        }

        return remoteHostConnection;

    }
    private <K> Future<K> getFuture(Callable<K> r) {

        return ThreadPool.submit(r);

    }

    //endregion

    //region SETTERS PROPERTIES

    private BandwidthLimitation     DownloadBandwidthLimit  = BandwidthLimitation.UNLIMITED;
    private BandwidthLimitation     UploadBandwidthLimit    = BandwidthLimitation.UNLIMITED;

    private String                  CurrentUserAgentString  = "Java WebClient";
    private int                     BufferSize              = 1024 * 4;
    private Charset                 Encoding                = StandardCharsets.UTF_8;
    private HashMap<String, String> internalHeaders         = new HashMap<>();
    private LimitationMode          BandwidthLimitationMode = LimitationMode.Global;

    //endregion

    //region SETTERS

    /**
     * Defines the maximum bytes per second limit for download operations. Default is unlimited.
     * @throws IllegalBandwidthException
     */
    public void setDownloadBandwidthLimit(BandwidthLimitation downloadLimit) throws IllegalBandwidthException {

        if (downloadLimit == null)
            this.DownloadBandwidthLimit = BandwidthLimitation.UNLIMITED;

        if (downloadLimit.getMaximumBytesSecond() < 1)
            throw new IllegalBandwidthException("The specified byte rate is negative");

        this.DownloadBandwidthLimit = downloadLimit;

    }

    /**
     * Defines the maximum bytes per second limit for upload operations. Default is unlimited.
     * @throws IllegalBandwidthException
     */
    public void setUploadBandwidthLimit(BandwidthLimitation uploadLimit) throws IllegalBandwidthException {

        if (uploadLimit == null)
            this.DownloadBandwidthLimit = BandwidthLimitation.UNLIMITED;

        if (uploadLimit.getMaximumBytesSecond() < 1)
            throw new IllegalBandwidthException("The specified byte rate is negative");

        this.UploadBandwidthLimit = uploadLimit;

    }

    /**
     * Defines the size in bytes of the buffer that is used to copy streams in both upload/download operations. Default size is 4096b bytes .
     * @throws InvalidBufferSizeException If the bufferSize parameter is not a power of 2.
     */
    public void setBufferSize(int bufferSize) throws InvalidBufferSizeException {

        if (!BufferUtility.IsValidBufferSize(bufferSize))
            throw new InvalidBufferSizeException("bufferSize has to be a power of 2");

        this.BufferSize = bufferSize;

    }

    /**
     * Sets the "User-Agent" header value in every request.
     */
    public void setUserAgent(String userAgent) {

        if (userAgent == null) {

            CurrentUserAgentString = "";

        }else {

            CurrentUserAgentString = userAgent;

        }

    }

    /**
     * Sets the encoding used to encode and decode strings.
     */
    public void setEncoding(Charset charset) {

        this.Encoding = charset;

    }

    /**
     * Sets the bandwidth limitation mode.
     */
    public void setLimitationMode(LimitationMode mode) {

        this.BandwidthLimitationMode = mode;

    }

    //endregion

    //region GETTERS

    /**
     * @return A mutable HashMap containing the "additional" headers that will be sent for every request.
     */
    public HashMap<String, String> Headers() {

        return internalHeaders;

    }

    /**
     * @return A boolean that indicates if the current WebClient instance is working on any kind of task.
     */
    public boolean isBusy() {

        return TaskCounter.getRunningTaskCount() > 0;

    }

    //endregion

    //region PUBLIC FUNCTIONS

    /**
     * Sends a GET request to the specified URL.
     * @return The read bytes from the response content.
     * @throws IOException
     */
    public byte[] downloadData(URL url) throws IOException {

        return internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Synchronous);

    }

    /**
     * Sends a GET request to the specified URL.
     * @return The read string decoded with the specified encoding.
     * @throws IOException
     */
    public String downloadString(URL url) throws IOException {

        return internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Synchronous);

    }

    /**
     * Sends a GET request to the specified URL and saves the response bytes to a file.
     * @throws FileNotEradicableException If the file already exists and can not be deleted.
     * @throws IOException
     */
    public void downloadFile(URL url, File localFile) throws FileNotEradicableException, IOException {

        internalDownloadFile(url, localFile, RequestSpecification.DownloadFile, SyncType.Synchronous);

    }

    /**
     * Sends a GET request to the specified URL.
     * @return Future&lt;byte[]&gt; that will contain the response bytes when read.
     */
    public Future<byte[]> downloadDataAsync(URL url) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Asynchronous));

    }

    /**
     * Sends a GET request to the specified URL.
     * @return Future&lt;String&gt; that will contain the response string when read and decoded with the specified encoding.
     */
    public Future<String> downloadStringAsync(URL url) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Asynchronous));

    }

    /**
     * Sends a GET request to the specified URL.
     * @return Future&lt;Boolean&gt; that indicates if no error were encountered.
     */
    public Future<Boolean> downloadFileAsync(URL remoteFileUrl, File localFile) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalDownloadFile(remoteFileUrl, localFile, RequestSpecification.DownloadFile, SyncType.Asynchronous));

    }

    /**
     * Sends a POST request to the specified URL with the specified byte array as the request body.
     * @return The response bytes.
     * @throws IOException
     */
    public byte[] uploadData(URL url, byte[] data) throws IOException {

        return internalUploadData(url, data, RequestSpecification.PostBytes, SyncType.Synchronous);

    }

    /**
     * Sends a POST request to the specified URL with the specified string encoded with the specified encoding as the request body.
     * @return The response string decoded with the specified encoding.
     * @throws IOException
     */
    public String uploadString(URL url, String string) throws IOException {

        return internalUploadString(url, string, RequestSpecification.PostString, SyncType.Synchronous);

    }

    /**
     * Sends a POST request to the specified URL with the specified byte array as the request body.
     * @return Future&lt;byte[]&gt; that will contain the response bytes when read.
     */
    public Future<byte[]> uploadDataAsync(URL url, byte[] data) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalUploadData(url, data, RequestSpecification.PostBytes, SyncType.Asynchronous));

    }

    /**
     * Sends a POST request to the specified URL with the specified string as the request body.
     * @return Future&lt;String&gt; that will contain the response string when read and decoded with the specified encoding.
     */
    public Future<String> uploadStringAsync(URL url, String string) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalUploadString(url, string, RequestSpecification.PostString, SyncType.Asynchronous));

    }

    //endregion

    //region INTERNAL FUNCTIONS

    private byte[] internalDownloadData(URL url, RequestSpecification specification, SyncType syncType) throws IOException, IOException {

        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }

        HttpURLConnection remoteConnection = getConnection(url, HttpMethod.GET, specification);

        InputStream connectionInputStream = null;

        connectionInputStream = remoteConnection.getInputStream();

        ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();

        StreamUtility.copyStream(DownloadBandwidthLimit.getMaximumBytesSecond(), this.BufferSize, this.TaskCounter, this.BandwidthLimitationMode,connectionInputStream, finalOutput);

        TaskCounter.decrementRunningTaskCount();

        return finalOutput.toByteArray();

    }
    private String internalDownloadString(URL url, RequestSpecification specification, SyncType syncType) throws IOException {

        byte[] data = internalDownloadData(url, specification, syncType);

        if (data.length == 0) {return "";}

        return new String(data, this.Encoding);

    }
    private boolean internalDownloadFile(URL fileUrl, File localFile, RequestSpecification specification, SyncType syncType) throws FileNotEradicableException, IOException {

        byte[] fileData = internalDownloadData(fileUrl, specification, syncType);

        if (localFile.exists()) {

            if (!localFile.delete()) {

                FileNotEradicableException.ThrowFromFile(localFile);

            }

        }

        try (FileOutputStream fileOutput = new FileOutputStream(localFile)) {

            fileOutput.write(fileData);

        }

        return true;

    }

    private byte[] internalUploadData(URL url, byte[] data, RequestSpecification specification, SyncType syncType) throws IOException {

        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }

        HttpURLConnection remoteConnection = getConnection(url, HttpMethod.POST, specification);

        remoteConnection.setFixedLengthStreamingMode(data.length);

        InputStream connectionInputStream = null;
        OutputStream connectionOutputStream = null;

        connectionOutputStream = remoteConnection.getOutputStream();

        ByteArrayInputStream inputDataStream = new ByteArrayInputStream(data);

        StreamUtility.copyStream(this.UploadBandwidthLimit.getMaximumBytesSecond(), this.BufferSize, this.TaskCounter, this.BandwidthLimitationMode, inputDataStream, connectionOutputStream);

        StreamUtility.closeStreamTunnel(inputDataStream, connectionOutputStream);

        connectionInputStream = remoteConnection.getInputStream();

        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

        StreamUtility.copyStream(this.DownloadBandwidthLimit.getMaximumBytesSecond(), this.BufferSize, this.TaskCounter, this.BandwidthLimitationMode, connectionInputStream, responseOutputStream);

        byte[] responseData = responseOutputStream.toByteArray();

        StreamUtility.closeStreamTunnel(connectionInputStream, responseOutputStream);

        TaskCounter.decrementRunningTaskCount();

        return responseData;

    }
    private String internalUploadString(URL url, String string, RequestSpecification specification, SyncType syncType) throws IOException {

        byte[] stringBytes = string.getBytes(this.Encoding);
        byte[] response = internalUploadData(url, stringBytes, specification, syncType);

        if (response.length == 0) {return "";}

        return new String(response, this.Encoding);

    }
    private void internalUploadFile(URL url, File file, RequestSpecification specification, SyncType syncType) {

        throw new NotImplementedException();

    }

    //endregion

}