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
public class UserAgentFilter implements ClientRequestFilter {
    static final String userAgent = "soda-java/" + APIVersion.version;

    @Override
    public void filter(ClientRequestContext cr) throws IOException
    {
        if (!cr.getHeaders().containsKey(HttpLowLevel.USER_AGENT_HEADER)) {
            cr.getHeaders().add(HttpLowLevel.USER_AGENT_HEADER, userAgent);
        }
    }
}
