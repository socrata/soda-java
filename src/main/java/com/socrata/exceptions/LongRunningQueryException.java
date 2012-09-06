package com.socrata.exceptions;

import java.net.URI;

/**
 * Thrown if an operation takes a long time, and needs to be completed
 * asynchronously.  In this case, a GET operation of the location will be able
 * to check for the tasks completion.
 */
public class LongRunningQueryException extends Exception
{
    public final URI location;
    public final long   timeToRetry;

    public LongRunningQueryException(URI location, long timeToRetry)
    {
        this.location = location;
        this.timeToRetry = timeToRetry;
    }
}
