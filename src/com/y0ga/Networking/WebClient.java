package com.y0ga.Networking;

import com.y0ga.Networking.Exceptions.IllegalBandwidthException;
import com.y0ga.Networking.Exceptions.InvalidBufferSizeException;
import com.y0ga.Networking.Asynchronous.AsyncTask;

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

        HttpURLConnection remoteHostConnection = (HttpURLConnection) url.openConnection();

        remoteHostConnection.setRequestMethod(method.getMethodString());

        remoteHostConnection.setDoOutput(method.getDoOutput());
        remoteHostConnection.setDoInput(true);

        remoteHostConnection.setRequestProperty("User-Agent", CurrentUserAgentString);

        specification.SetHeaders(remoteHostConnection);

        for (String headerKey : this.InternalHeaders.keySet()) {

            String headerValue = this.InternalHeaders.get(headerKey);

            remoteHostConnection.setRequestProperty(headerKey, headerValue);

        }

        return remoteHostConnection;

    }
    private HttpIOStreamTunnel createTunnel(URL url, HttpMethod method, RequestSpecification specification, InputStream input, OutputStream output) throws IOException {
        
        HttpURLConnection connection = getConnection(url, method, specification);
        
        HttpIOStreamTunnel tunnel;
        
        switch (method) {
            
            case GET:
                tunnel = new HttpIOStreamTunnel(connection, connection.getInputStream(), output);
                break;
            
            case POST:
                tunnel = new HttpIOStreamTunnel(connection, input, connection.getOutputStream());
                break;
            
            default:
                throw new IllegalStateException("No http method other than GET or POST is currently supported.");
            
        }
        
        return tunnel;
        
    }
    
    private StreamCopySettings getCopySettings(OperationType operationType) {

        long maxBytes = (operationType == OperationType.Download) ? DownloadBandwidthLimit.getMaximumBytesSecond() : UploadBandwidthLimit.getMaximumBytesSecond();

        return new StreamCopySettings(maxBytes, this.BufferSize, this.TaskCounter, this.BandwidthLimitationMode);

    }
    
    private <T> AsyncTask<T> getAsyncTask(Callable<T> callable) {
    
        return new AsyncTask(ThreadPool.submit(callable));
    
    }
    
    //endregion

    //region PROPERTIES

    private BandwidthLimitation     DownloadBandwidthLimit  = BandwidthLimitation.UNLIMITED;
    private BandwidthLimitation     UploadBandwidthLimit    = BandwidthLimitation.UNLIMITED;

    private String                  CurrentUserAgentString  = "Java WebClient";
    private int                     BufferSize              = 1024 * 4;
    private Charset                 Encoding                = StandardCharsets.UTF_8;
    private LimitationMode          BandwidthLimitationMode = LimitationMode.Global;

    private final HashMap<String, String> InternalHeaders = new HashMap<>();

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
     * Defines the size in bytes of the buffer that is used to copy streams in both upload/download operations. Default size is 4096 bytes .
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

        } else {

            CurrentUserAgentString = userAgent;

        }

    }

    /**
     * Sets the encoding used to encode and decode strings.
     */
    public void setEncoding(Charset charset) {

        if (charset == null)
            return;

        this.Encoding = charset;

    }

    /**
     * Sets the bandwidth limitation mode.
     */
    public void setLimitationMode(LimitationMode mode) {

        if (mode == null)
            return;

        this.BandwidthLimitationMode = mode;

    }

    //endregion

    //region GETTERS

    /**
     * @return A HashMap containing the "additional" headers that will be sent for every request.
     */
    public HashMap<String, String> Headers() {

        return InternalHeaders;

    }

    /**
     * @return A boolean that indicates if the current WebClient instance is working on any kind of task.
     * @see #getRunningTaskCount()
     */
    public boolean isBusy() {

        return TaskCounter.getRunningTaskCount() > 0;

    }

    /**
     * @return The count of asynchronous and synchronous tasks running within the current WebClient instance.
     * @see #isBusy()
     */
    public int getRunningTaskCount() { return TaskCounter.getRunningTaskCount(); }

    //endregion

    //region PUBLIC FUNCTIONS
    
    //region SYNCHRONOUS
    
    public void downloadData(URL url, OutputStream output) throws IOException {
    
        internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Synchronous, output);
    
    }
    public String downloadString(URL url) throws IOException {

        return internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Synchronous);

    }
    public void downloadFile(URL url, File localFile) throws IOException {

        internalDownloadFile(url, localFile, RequestSpecification.DownloadFile, SyncType.Synchronous);

    }
    
    public void uploadData(URL url, InputStream data, OutputStream output) throws IOException {
        
        internalUploadData(url, data, output, RequestSpecification.PostBytes, SyncType.Synchronous);
        
    }
    public String uploadString(URL url, String string) throws IOException {
        
        return internalUploadString(url, string, RequestSpecification.PostString, SyncType.Synchronous);
        
    }
    
    //endregion
    
    //region ASYNCHRONOUS
    
    public AsyncTask downloadDataAsync(URL url, OutputStream output) {
        
        TaskCounter.incrementRunningTaskCount();

        return getAsyncTask(() -> internalDownloadData(url, RequestSpecification.DownloadBytes, SyncType.Asynchronous, output));
        
    }
    public AsyncTask<String> downloadStringAsync(URL url) {

        TaskCounter.incrementRunningTaskCount();

        return getAsyncTask(() -> internalDownloadString(url, RequestSpecification.DownloadString, SyncType.Asynchronous));

    }
    public AsyncTask downloadFileAsync(URL remoteFileUrl, File localFile) {

        TaskCounter.incrementRunningTaskCount();

        return getAsyncTask(() -> internalDownloadFile(remoteFileUrl, localFile, RequestSpecification.DownloadFile, SyncType.Asynchronous));

    }
    
    public AsyncTask uploadDataAsync(URL url, InputStream input, OutputStream output) {
        
        TaskCounter.incrementRunningTaskCount();
        
        return getAsyncTask(() -> internalUploadData(url, input, output, RequestSpecification.PostBytes, SyncType.Asynchronous));
        
    }
    public AsyncTask<String> uploadStringAsync(URL url, String string) {
        
        TaskCounter.incrementRunningTaskCount();
        
        return getAsyncTask(() -> internalUploadString(url, string, RequestSpecification.PostString, SyncType.Asynchronous));
        
    }
    
    //endregion

    //endregion

    //region INTERNAL FUNCTIONS

    private Void internalDownloadData(URL url, RequestSpecification specification, SyncType syncType, OutputStream output) throws IOException {

        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }
        
        try {
    
            createTunnel(url, HttpMethod.GET, specification, null, output)
                        .copy(getCopySettings(OperationType.Download))
                        .close(false, true);
    
            return null;
            
        } catch (IOException ex) {throw ex;}
        finally {
            TaskCounter.decrementRunningTaskCount();
        }
        
    }
    private String internalDownloadString(URL url, RequestSpecification specification, SyncType syncType) throws IOException {
    
        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }
    
        ByteArrayOutputStream stringBytes = new ByteArrayOutputStream();
        
        try {
        
            createTunnel(url, HttpMethod.GET, specification, null, stringBytes)
                    .copy(getCopySettings(OperationType.Download))
                    .close(true, true);
    
            return stringBytes.toString(this.Encoding.name());
    
        } catch (Exception ex) {throw ex;}
        finally {
            TaskCounter.decrementRunningTaskCount();
        }

    }
    private Void internalDownloadFile(URL fileUrl, File localFile, RequestSpecification specification, SyncType syncType) throws IOException {
    
        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }
    
        FileOutputStream fileOutputStream = new FileOutputStream(localFile);
        
        try {
    
            createTunnel(fileUrl, HttpMethod.GET, specification, null, fileOutputStream)
                    .copy(getCopySettings(OperationType.Download))
                    .close(true, true);
            
            return null;
            
        } catch (Exception ex) {throw ex;}
        finally {
            TaskCounter.decrementRunningTaskCount();
        }
        
    }

    private Void internalUploadData(URL url, InputStream data, OutputStream output, RequestSpecification specification, SyncType syncType) throws IOException {

        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }
        
        HttpIOStreamTunnel reqTunnel = null;

        try {
            
            reqTunnel = createTunnel(url, HttpMethod.POST, specification, data, null)
                                 .copy(getCopySettings(OperationType.Upload));
            
            new HttpIOStreamTunnel(reqTunnel.getUnderlyingInput(), output)
                                  .copy(getCopySettings(OperationType.Download))
                                  .close(false, true);
    
            return null;
    
        } finally {
            TaskCounter.decrementRunningTaskCount();
        }
        
    }
    private String internalUploadString(URL url, String string, RequestSpecification specification, SyncType syncType) throws IOException {
    
        if (syncType == SyncType.Synchronous) { TaskCounter.incrementRunningTaskCount(); }
    
        HttpIOStreamTunnel reqTunnel = null;
    
        try {
        
            ByteArrayInputStream inputStringBytes = new ByteArrayInputStream(string.getBytes(this.Encoding));
        
            reqTunnel = createTunnel(url, HttpMethod.POST, specification, inputStringBytes, null)
                                    .copy(getCopySettings(OperationType.Upload));
        
            ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
        
            new HttpIOStreamTunnel(reqTunnel.getUnderlyingInput(), responseOutputStream)
                    .copy(getCopySettings(OperationType.Download))
                    .close(true, true);
        
            return responseOutputStream.toString(this.Encoding.name());
        
        } finally {
            TaskCounter.decrementRunningTaskCount();
        }
    
    }
    private Void internalUploadFile(URL url, File file, RequestSpecification specification, SyncType syncType) {

        throw new NotImplementedException();

    }

    //endregion

    //region PRIVATE ENUMS
    
    private enum OperationType { Download, Upload }
    private enum SyncType { Synchronous, Asynchronous }
    
    //endregion
    
}