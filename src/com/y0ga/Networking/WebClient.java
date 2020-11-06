package com.y0ga.Networking;

import com.y0ga.Networking.Enums.*;
import com.y0ga.Networking.BandwidthLimitation;
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

    private byte[] internalDownloadData(URL url, RequestSpecification specification) throws ConnectionException {

        TaskInfo.incrementRunningTaskCount();

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

        return finalOutput.toByteArray();

    }
    private String internalDownloadString(URL url, RequestSpecification specification) throws ConnectionException {

        byte[] data = internalDownloadData(url, specification);

        if (data.length == 0) {return "";}

        return new String(data, this.Encoding);

    }
    private boolean internalDownloadFile(URL fileUrl, File localFile, RequestSpecification specification) throws FileNotEradicableException, ConnectionException {

        byte[] fileData = internalDownloadData(fileUrl, specification);

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

        try {

            return internalDownloadData(url, RequestSpecification.DownloadBytes);

        } catch (Exception ex) {throw ex;}
        finally {

            TaskInfo.decrementRunningTaskCount();

        }

    }
    public String downloadString(URL url) throws ConnectionException {

        try {

            return internalDownloadString(url, RequestSpecification.DownloadString);

        } catch (Exception ex) {throw ex;}
        finally {

            TaskInfo.decrementRunningTaskCount();

        }

    }
    public boolean downloadFile(URL url, File localFile) throws FileNotEradicableException, ConnectionException {

        try {

            return internalDownloadFile(url, localFile, RequestSpecification.DownloadFile);

        } catch (Exception ex) {throw ex;}
        finally {

            TaskInfo.decrementRunningTaskCount();

        }

    }

    public Future<byte[]> downloadDataAsync(URL url) {

        ExecutorService executor = getNewExecutor();

        Future<byte[]> future = executor.submit(() -> downloadData(url));

        executor.shutdown();

        return future;

    }
    public Future<String> downloadStringAsync(URL url) {

        ExecutorService executor = getNewExecutor();

        Future<String> future = executor.submit(() -> downloadString(url));

        executor.shutdown();

        return future;
    }
    public Future<Boolean> downloadFileAsync(URL remoteFileUrl, File localFile) {

        ExecutorService executor = getNewExecutor();

        Future<Boolean> future = executor.submit(() -> downloadFile(remoteFileUrl, localFile));

        executor.shutdown();

        return future;

    }

    private byte[] internalUploadData(URL url, byte[] data, RequestSpecification specification) throws ConnectionException {

        TaskInfo.incrementRunningTaskCount();

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

        return responseData;

    }
    private String internalUploadString(URL url, String string, RequestSpecification specification) throws ConnectionException {

        byte[] stringBytes = string.getBytes(this.Encoding);
        byte[] response = internalUploadData(url, stringBytes, specification);

        if (response.length == 0) {return "";}

        return new String(response, this.Encoding);

    }
    private void internalUploadFile(URL url, File file, RequestSpecification specification) {

        throw new NotImplementedException();

    }

    public byte[] uploadData(URL url, byte[] data) throws ConnectionException {

        try {

            return internalUploadData(url, data, RequestSpecification.PostBytes);

        } catch (Exception ex) {throw ex;}
        finally {

            TaskInfo.decrementRunningTaskCount();

        }

    }
    public String uploadString(URL url, String string) throws ConnectionException {

        try {

            return internalUploadString(url, string, RequestSpecification.PostString);

        } catch (Exception ex) {throw ex;}
        finally {

            TaskInfo.decrementRunningTaskCount();

        }

    }

    public Future<byte[]> uploadDataAsync(URL url, byte[] data) throws ConnectionException {

        ExecutorService executor = getNewExecutor();

        Future<byte[]> future = executor.submit(() -> uploadData(url, data));

        executor.shutdown();

        return future;

    }
    public Future<String> uploadStringAsync(URL url, String string) throws ConnectionException {

        ExecutorService executor = getNewExecutor();

        Future<String> future = executor.submit(() -> uploadString(url, string));

        executor.shutdown();

        return future;

    }

    private ExecutorService getNewExecutor() {

        return Executors.newSingleThreadExecutor();

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

}