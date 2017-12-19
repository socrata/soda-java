package com.socrata.model.requests;

import java.io.IOException;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import javax.ws.rs.core.Response;

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

    abstract public Response  issueRequest() throws LongRunningQueryException, SodaError;
}
