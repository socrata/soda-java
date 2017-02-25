package com.socrata.api;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.Collections;

/**
 * The Assets resource returns JSON as media type "text/plain" so Jackson will not un-marshall an
 * {@link com.socrata.model.importer.AssetResponse} correctly. This filter re-writes response
 * objects from the assets API so that they use the correct MIME type
 */
@Provider
public class AssetRequestFilter extends ClientFilter {

    @Override
    public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
        final String path = cr.getURI().getPath();
        final ClientResponse response = getNext().handle(cr);

        final MediaType type = response.getType();

        if (path.endsWith("assets") &&
                response.hasEntity() &&
                type.isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
            response.getHeaders().put("Content-Type", Collections.singletonList(MediaType.APPLICATION_JSON));
        }
        return response;
    }
}
