package com.socrata.api;

import java.io.IOException;

import com.google.common.base.Preconditions;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import javax.annotation.Nonnull;

/**
 * A client filter for Jersey that will add the SODA2 request ID to the responses.
 */
public class SodaRequestIdFilter implements ClientRequestFilter {
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
    public void filter(ClientRequestContext cr) throws IOException
    {
        if (!cr.getHeaders().containsKey(HttpLowLevel.SOCRATA_REQUEST_ID_HEADER)) {
            cr.getHeaders().add(HttpLowLevel.SOCRATA_REQUEST_ID_HEADER, requestId);
        }
    }
}
