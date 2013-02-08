package com.socrata.model.requests;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.sun.jersey.api.client.ClientResponse;

/**
 *
 * An abstract class that defines a Soda request.  This allows a request to
 * be replayed at a later time.
 */
abstract public class SodaRequest<T>
{
    public final String resourceId;
    public final T payload;

    protected SodaRequest(String resourceId, T payload)
    {
        this.resourceId = resourceId;
        this.payload = payload;
    }

    abstract public ClientResponse  issueRequest() throws LongRunningQueryException, SodaError;
}
