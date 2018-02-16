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
    public       int status = 500;



    public SodaError(SodaErrorResponse sodaErrorResponse, int status)
    {
        super(sodaErrorResponse.message);
        this.sodaErrorResponse = sodaErrorResponse;
        this.status = status;
    }

    public SodaError(SodaErrorResponse sodaErrorResponse) {
        this(sodaErrorResponse, 500);
    }


   public SodaError(String error, Throwable throwable) {
       super(error, throwable);
       this.sodaErrorResponse = new SodaErrorResponse("", error, "", null);
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
