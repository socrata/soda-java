package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.LongRunningRequestStatusCheckException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.requests.SodaRequest;
import com.sun.jersey.api.client.ClientHandlerException;

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

    public R checkStatus(HttpLowLevel httpLowLevel, int retries, long intervalMs) throws SodaError, InterruptedException {
        for (int i = 0; i <= retries; i++) {
            try {
                return httpLowLevel.getAsyncResults(
                    longRunningQueryException.location,
                    longRunningQueryException.timeToRetry,
                    Integer.MAX_VALUE,
                    cls,
                    sodaRequest);
            } catch (ClientHandlerException exception) {
                if (i >= retries) {
                    throw new LongRunningRequestStatusCheckException(exception, this);
                }
            } catch (SodaError sodaError) {
                if (i >= retries) {
                    sodaError.setLongRunningRequest(this);
                    throw sodaError;
                }
            }
            Thread.sleep(intervalMs);
        }
        throw new IllegalArgumentException("Argument - retires must be >= 0");
    }
}
