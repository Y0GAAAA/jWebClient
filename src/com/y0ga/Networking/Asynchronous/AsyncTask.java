package com.y0ga.Networking.Asynchronous;

import java.util.concurrent.*;

/**
 * An awaitable asynchronous task wrapping a {@link Future} that can be retrieved using {@link #getUnderlyingFuture()}
 * @param <T> The return type of the Future.
 */
public class AsyncTask<T> {

    private Future<T> underlyingFuture;
    
    /**
     * Creates a new instance of the class from an existing {@link Future}
     */
    public AsyncTask(Future<T> f) {
        
        this.underlyingFuture = f;

    }
    
    /**
     * Waits for the operation to complete and returns the result.
     * @throws InterruptedException if the task was cancelled
     * @throws ExecutionException if the computation threw an exception
     */
    public T await() throws InterruptedException, ExecutionException {
        
        return underlyingFuture.get();
    
    }
    
    /**
     * Waits for the operation to complete within the specified delay and returns the result.
     * @throws InterruptedException if the task was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the task timed out
     */
    public T await(long timeout, TimeUnit unit)  throws InterruptedException, ExecutionException, TimeoutException {
        
        return underlyingFuture.get(timeout, unit);
        
    }
    
    /**
     * @return the underlying future that this class is wrapping
     */
    public Future<T> getUnderlyingFuture() { return underlyingFuture; }

}
