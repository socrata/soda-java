package com.socrata.exceptions;

import com.socrata.api.LongRunningRequest;
import com.socrata.model.SodaErrorResponse;

/**
 * Base class of all errors thrown from Soda.
 */
public class SodaError extends Exception
{
    public final SodaErrorResponse sodaErrorResponse;
    public       LongRunningRequest longRunningRequest;

    public SodaError(SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse.message);
        this.sodaErrorResponse = sodaErrorResponse;
    }

    public SodaError(String error)
    {
        super(error);
        this.sodaErrorResponse = new SodaErrorResponse("", error, "", null);
    }

    public SodaError(Throwable throwable)
    {
        super(throwable);
        this.sodaErrorResponse = new SodaErrorResponse("", "", "", null);
    }

    public <T, R> LongRunningRequest<T, R> getLongRunningRequest()
    {
        return longRunningRequest;
    }

    public void setLongRunningRequest(LongRunningRequest longRunningRequest)
    {
        this.longRunningRequest = longRunningRequest;
    }
}
