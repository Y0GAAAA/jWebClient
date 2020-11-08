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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WebClient {

    //PRIVATE

    private final ExecutorService ThreadPool = Executors.newCachedThreadPool();
    private final ConcurrentTaskCounter TaskCounter = new ConcurrentTaskCounter();

    //EXPOSED PROPERTIES

    private BandwidthLimitation     DownloadBandwidthLimit  = BandwidthLimitation.Unlimited;
    private BandwidthLimitation     UploadBandwidthLimit    = BandwidthLimitation.Unlimited;

    private String                  CurrentUserAgentString  = "Java WebClient";
    private int                     BufferSize              = 1024 * 4;
    private Charset                 Encoding                = StandardCharsets.UTF_8;
    private HashMap<String, String> internalHeaders         = new HashMap<>();
    private LimitationMode          BandwidthLimitationMode = LimitationMode.Global;

    //SETTERS

    public void setDownloadBandwidthLimit(BandwidthLimitation downloadLimit) throws NullBandwidthException {

        if (downloadLimit == null || downloadLimit.getMaximumBytesSecond() < 1)
            throw new NullBandwidthException("downloadLimit is null or it's byte rate is negative");

        this.DownloadBandwidthLimit = downloadLimit;

    }
    public void setUploadBandwidthLimit(BandwidthLimitation uploadLimit) throws NullBandwidthException {

        if (uploadLimit == null || uploadLimit.getMaximumBytesSecond() < 1)
            throw new NullBandwidthException("uploadLimit is null or it's byte rate is negative");

        this.UploadBandwidthLimit = uploadLimit;

    }

    public void setBufferSize(int bufferSize) throws InvalidBufferSizeException {

        if (!BufferUtility.IsValidBufferSize(bufferSize))
            throw new InvalidBufferSizeException("bufferSize has to be a power of 2");

        this.BufferSize = bufferSize;

    }
    public void setUserAgent(String userAgent) {

        if (userAgent == null) {

            CurrentUserAgentString = "";

        }else {

            CurrentUserAgentString = userAgent;

        }

    }
    public void setEncoding(Charset charset) {

        this.Encoding = charset;

    }
    public void setLimitationMode(LimitationMode mode) {

        this.BandwidthLimitationMode = mode;

    }

    //GETTERS

    public HashMap<String, String> AdditionalHeaders() {

        return internalHeaders;

    }
    public boolean isBusy() {

        return TaskCounter.getRunningTaskCount() > 0;

    }

    //FUNCTIONS

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

        } catch (Exception ex) {return false;}

        return true;

    }

    public byte[] downloadData(URL url) throws IOException {

        return internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Synchronous);

    }
    public String downloadString(URL url) throws IOException {

        return internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Synchronous);

    }
    public boolean downloadFile(URL url, File localFile) throws FileNotEradicableException, IOException {

        return internalDownloadFile(url, localFile, RequestSpecification.DownloadFile, SyncType.Synchronous);

    }

    public Future<byte[]> downloadDataAsync(URL url) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Asynchronous));

    }
    public Future<String> downloadStringAsync(URL url) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Asynchronous));

    }
    public Future<Boolean> downloadFileAsync(URL remoteFileUrl, File localFile) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalDownloadFile(remoteFileUrl, localFile, RequestSpecification.DownloadFile, SyncType.Asynchronous));

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

    public byte[] uploadData(URL url, byte[] data) throws IOException {

        return internalUploadData(url, data, RequestSpecification.PostBytes, SyncType.Synchronous);

    }
    public String uploadString(URL url, String string) throws IOException {

        return internalUploadString(url, string, RequestSpecification.PostString, SyncType.Synchronous);

    }

    public Future<byte[]> uploadDataAsync(URL url, byte[] data) {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalUploadData(url, data, RequestSpecification.PostBytes, SyncType.Asynchronous));

    }
    public Future<String> uploadStringAsync(URL url, String string) throws IOException {

        TaskCounter.incrementRunningTaskCount();

        return getFuture(() -> internalUploadString(url, string, RequestSpecification.PostString, SyncType.Asynchronous));

    }

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

}