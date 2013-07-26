package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.requests.SodaRequest;

/**
 * This class holds a long running request ticket so that users can check status despite of
 * possible network errors between submission and completion.
 */

public class LongRunningRequest<T, R>
{
    private LongRunningQueryException longRunningQueryException;
    private SodaRequest<T> sodaRequest;
    private Class<R> cls;

    public LongRunningRequest(LongRunningQueryException longRunningQueryException,  Class<R> cls, SodaRequest<T> sodaRequest)
    {
        this.longRunningQueryException = longRunningQueryException;
        this.sodaRequest = sodaRequest;
        this.cls = cls;
    }

    public R checkStatus(HttpLowLevel httpLowLevel) throws SodaError, InterruptedException {
        LongRunningQueryException e = longRunningQueryException;
        return httpLowLevel.getAsyncResults(e.location, e.timeToRetry, Integer.MAX_VALUE,  cls, sodaRequest);
    }
}
