package com.socrata.model.requests;

import javax.ws.rs.core.MediaType;

/**
 * Defines a Soda request that has a Mime type associated with it.
 */
abstract public class SodaTypedRequest<T> extends SodaRequest<T>
{
    final public MediaType mediaType;

    public SodaTypedRequest(String resourceId, T payload, MediaType mediaType)
    {
        super(resourceId, payload);
        this.mediaType = mediaType;
    }
}
