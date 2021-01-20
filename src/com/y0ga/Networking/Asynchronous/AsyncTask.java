package com.y0ga.Networking.Asynchronous;

import java.util.concurrent.*;

public class AsyncTask<T> {

    private Future<T> underlyingFuture;
    
    public AsyncTask(Future<T> f) {
        
        this.underlyingFuture = f;
    
    }
    
    public T await() throws InterruptedException, ExecutionException {
        
        return underlyingFuture.get();
    
    }
    
    public T await(long timeout, TimeUnit unit)  throws InterruptedException, ExecutionException, TimeoutException {
        
        return underlyingFuture.get(timeout, unit);
        
    }
    
}
