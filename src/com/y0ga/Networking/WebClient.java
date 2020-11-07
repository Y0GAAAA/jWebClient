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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WebClient {

    //EXPOSED PROPERTIES

    private BandwidthLimitation     DownloadBandwidthLimit  = BandwidthLimitation.Unlimited;
    private BandwidthLimitation     UploadBandwidthLimit    = BandwidthLimitation.Unlimited;

    private String                  CurrentUserAgentString  = "Java WebClient";
    private int                     BufferSize              = 1024;
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

        return TaskInfo.getRunningTaskCount() > 0;

    }

    //FUNCTIONS

    private byte[] internalDownloadData(URL url, RequestSpecification specification, SyncType syncType) throws ConnectionException {

        if (syncType == SyncType.Synchronous) { TaskInfo.incrementRunningTaskCount(); }

        HttpURLConnection remoteConnection = getConnection(url, HttpMethod.GET, specification);

        if (remoteConnection == null) {
            return new byte[]{};
        }

        InputStream connectionInputStream = null;

        try {
            connectionInputStream = remoteConnection.getInputStream();
        } catch (IOException ignored) {
            return new byte[]{};
        }

        ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();

        StreamUtility.copyStream(DownloadBandwidthLimit.getMaximumBytesSecond(), this.BufferSize, this.BandwidthLimitationMode,connectionInputStream, finalOutput);

        TaskInfo.decrementRunningTaskCount();

        return finalOutput.toByteArray();

    }
    private String internalDownloadString(URL url, RequestSpecification specification, SyncType syncType) throws ConnectionException {

        byte[] data = internalDownloadData(url, specification, syncType);

        if (data.length == 0) {return "";}

        return new String(data, this.Encoding);

    }
    private boolean internalDownloadFile(URL fileUrl, File localFile, RequestSpecification specification, SyncType syncType) throws FileNotEradicableException, ConnectionException {

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

    public byte[] downloadData(URL url) throws ConnectionException {

        return internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Synchronous);

    }
    public String downloadString(URL url) throws ConnectionException {

        return internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Synchronous);

    }
    public boolean downloadFile(URL url, File localFile) throws FileNotEradicableException, ConnectionException {

        return internalDownloadFile(url, localFile, RequestSpecification.DownloadFile, SyncType.Synchronous);

    }

    public Future<byte[]> downloadDataAsync(URL url) {

        TaskInfo.incrementRunningTaskCount();

        return ThreadPool.submit(() -> internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Asynchronous));

    }
    public Future<String> downloadStringAsync(URL url) {

        TaskInfo.incrementRunningTaskCount();

        return ThreadPool.submit(() -> internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Asynchronous));

    }
    public Future<Boolean> downloadFileAsync(URL remoteFileUrl, File localFile) {

        TaskInfo.incrementRunningTaskCount();

        return ThreadPool.submit(() -> internalDownloadFile(remoteFileUrl, localFile, RequestSpecification.DownloadFile, SyncType.Asynchronous));

    }

    private byte[] internalUploadData(URL url, byte[] data, RequestSpecification specification, SyncType syncType) throws ConnectionException {

        if (syncType == SyncType.Synchronous) { TaskInfo.incrementRunningTaskCount(); }

        HttpURLConnection remoteConnection = getConnection(url, HttpMethod.POST, specification);

        if (remoteConnection == null) {
            return new byte[]{};
        }

        remoteConnection.setFixedLengthStreamingMode(data.length);

        InputStream connectionInputStream = null;
        OutputStream connectionOutputStream = null;

        try {

            connectionOutputStream = remoteConnection.getOutputStream();

        } catch (IOException ex) {
            return new byte[]{};
        }

        ByteArrayInputStream inputDataStream = new ByteArrayInputStream(data);

        StreamUtility.copyStream(this.UploadBandwidthLimit.getMaximumBytesSecond(), this.BufferSize, this.BandwidthLimitationMode, inputDataStream, connectionOutputStream);

        StreamUtility.closeStreamTunnel(inputDataStream, connectionOutputStream);

        try {

            connectionInputStream = remoteConnection.getInputStream();

        } catch (IOException ex) {
            return new byte[]{};
        }

        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

        StreamUtility.copyStream(this.DownloadBandwidthLimit.getMaximumBytesSecond(), this.BufferSize, this.BandwidthLimitationMode, connectionInputStream, responseOutputStream);

        byte[] responseData = responseOutputStream.toByteArray();

        StreamUtility.closeStreamTunnel(connectionInputStream, responseOutputStream);

        TaskInfo.decrementRunningTaskCount();

        return responseData;

    }
    private String internalUploadString(URL url, String string, RequestSpecification specification, SyncType syncType) throws ConnectionException {

        byte[] stringBytes = string.getBytes(this.Encoding);
        byte[] response = internalUploadData(url, stringBytes, specification, syncType);

        if (response.length == 0) {return "";}

        return new String(response, this.Encoding);

    }
    private void internalUploadFile(URL url, File file, RequestSpecification specification, SyncType syncType) {

        throw new NotImplementedException();

    }

    public byte[] uploadData(URL url, byte[] data) throws ConnectionException {

        return internalUploadData(url, data, RequestSpecification.PostBytes, SyncType.Synchronous);

    }
    public String uploadString(URL url, String string) throws ConnectionException {

        return internalUploadString(url, string, RequestSpecification.PostString, SyncType.Synchronous);

    }

    public Future<byte[]> uploadDataAsync(URL url, byte[] data) throws ConnectionException {

        TaskInfo.incrementRunningTaskCount();

        return ThreadPool.submit(() -> internalUploadData(url, data, RequestSpecification.PostBytes, SyncType.Asynchronous));

    }
    public Future<String> uploadStringAsync(URL url, String string) throws ConnectionException {

        TaskInfo.incrementRunningTaskCount();

        return ThreadPool.submit(() -> internalUploadString(url, string, RequestSpecification.PostString, SyncType.Asynchronous));

    }

    private HttpURLConnection getConnection(URL url, HttpMethod method, RequestSpecification specification) {

        HttpURLConnection remoteHostConnection = null;

        try {

            remoteHostConnection = (HttpURLConnection) url.openConnection();

            remoteHostConnection.setRequestMethod(method.getMethodString());

            remoteHostConnection.setDoOutput(method.getDoOutput());
            remoteHostConnection.setDoInput(true);

            remoteHostConnection.setRequestProperty("User-Agent", CurrentUserAgentString);

            specification.SetHeaders(remoteHostConnection);

            if (!this.internalHeaders.isEmpty()) {

                for (String headerKey : this.internalHeaders.keySet()) {

                    String headerValue = this.internalHeaders.get(headerKey);

                    remoteHostConnection.setRequestProperty(headerKey, headerValue);

                }

            }

        } catch (Exception exc) {return null;}

        return remoteHostConnection;

    }

    private static final ExecutorService ThreadPool =  Executors.newCachedThreadPool();

}