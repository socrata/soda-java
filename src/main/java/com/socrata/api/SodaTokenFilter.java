package com.socrata.api;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.annotation.Nonnull;
import javax.ws.rs.core.HttpHeaders;

/**
 * A client filter for Jersey that will add the SODA2 token to the responses.
 */
public class SodaTokenFilter extends ClientFilter
{
    private final String token;

    /**
     * Constructor that sets the token to add to the requests.
     * @param token authorization token for accessing a SODA2 domain.
     */
    public SodaTokenFilter(@Nonnull String token)
    {
        Preconditions.checkNotNull(token, "Requires a Non-Null app token");
        this.token = token;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException
    {
        if (!cr.getMetadata().containsKey(HttpLowLevel.SOCRATA_TOKEN_HEADER)) {
            cr.getMetadata().add(HttpLowLevel.SOCRATA_TOKEN_HEADER, token);
        }
        return getNext().handle(cr);
    }
}
