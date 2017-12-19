package com.socrata.api;

import java.io.IOException;

import com.google.common.base.Preconditions;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import javax.annotation.Nonnull;
import javax.ws.rs.core.HttpHeaders;

/**
 * A client filter for Jersey that will add the SODA2 token to the responses.
 */
public class SodaTokenFilter implements ClientRequestFilter
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
    public void filter(ClientRequestContext cr) throws IOException
    {
        if (!cr.getHeaders().containsKey(HttpLowLevel.SOCRATA_TOKEN_HEADER)) {
            cr.getHeaders().add(HttpLowLevel.SOCRATA_TOKEN_HEADER, token);
        }
    }
}
