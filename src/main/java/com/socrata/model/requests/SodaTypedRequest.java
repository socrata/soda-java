package com.socrata.model.requests;

import javax.ws.rs.core.MediaType;
import com.socrata.utils.RowUpdateOption;

/**
 * Defines a Soda request that has a Mime type associated with it.
 */
abstract public class SodaTypedRequest<T> extends SodaRequest<T>
{
    final public MediaType mediaType;
    final public RowUpdateOption rowUpdateOption;

    public SodaTypedRequest(String resourceId, T payload, MediaType mediaType)
    {
        super(resourceId, payload);
        this.mediaType = mediaType;
        this.rowUpdateOption = new RowUpdateOption();
    }

    public SodaTypedRequest(String resourceId, T payload, MediaType mediaType, RowUpdateOption rowUpdateOption)
    {
      super(resourceId, payload);
      this.mediaType = mediaType;
      this.rowUpdateOption = rowUpdateOption;
    }
}
