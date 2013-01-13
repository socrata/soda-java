package com.socrata.exceptions;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * Thrown if an operation takes a long time, and needs to be completed
 * asynchronously.  In this case, a GET operation of the location will be able
 * to check for the tasks completion.
 *
 * There are two basic mechanisms Socrata uses for returning "future" results:
 * <ol>
 *     <li>Location and Retry-After are returned as headers, and the caller should go back to the URL denoted in Location
 *     for getting the future result.</li>
 *     <li>There is a {@code ticket} parameter in the body of the response.  In this case, the ticket should be added
 *     to the calling URL as a parameter to retry.</li>
 * </ol>
 *
 */
public class LongRunningQueryException extends Exception
{
    public final URI location;
    public final long   timeToRetry;
    public final String ticket;

    /**
     * @param location The URL to go to for getting the future result.  If this is {@code null}, use the ticket parameter to construct
     *                 a URL.
     * @param timeToRetry The time in milliseconds to wait until retrying.
     * @param ticket the ticket to use for polling for the future results.  This should ONLY be used in location is {@code null}.
     */
    public LongRunningQueryException(@Nullable final URI location, final long timeToRetry, @Nullable final String ticket)
    {
        this.location = location;
        this.timeToRetry = timeToRetry;
        this.ticket = ticket;
    }
}
