package com.socrata.api;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.annotation.Nonnull;

/**
 * A client filter for Jersey that will add the SODA2 request ID to the responses.
 */
public class SodaRequestIdFilter extends ClientFilter {
    private final String requestId;

    /**
     * Constructor that sets the token to add to the requests.
     * @param requestId a 32 character id unique to a single SODA 2 publish operation.
     */
    public SodaRequestIdFilter(@Nonnull String requestId)
    {
        Preconditions.checkNotNull(requestId, "Requires a Non-Null request id");
        this.requestId = requestId;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException
    {
        if (!cr.getMetadata().containsKey(HttpLowLevel.SOCRATA_REQUEST_ID_HEADER)) {
            cr.getMetadata().add(HttpLowLevel.SOCRATA_REQUEST_ID_HEADER, requestId);
        }
        return getNext().handle(cr);
    }
}
