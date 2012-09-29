package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;

/**
 * A base class that wraps some common SODA 2 behaviours around an HttpLowLevel object.
 *
 */
public class Soda2Base
{
    protected static final int    DEFAULT_MAX_RETRIES = 20;
    private final HttpLowLevel httpLowLevel;

    /**
     * Constructor.
     *
     * @param httpLowLevel the HttpLowLevel this uses to contact the server
     */
    public Soda2Base(HttpLowLevel httpLowLevel)
    {
        this.httpLowLevel = httpLowLevel;
    }

    /**
     * Method to check the async callbacks for new responses.
     *
     * @param uri the URI to go to for responses.
     * @param waitTime the time to wait until the first response
     * @param numRetries the total number of times to retry before failing.
     * @return the ClientReponse for a successful response.
     *
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    final public ClientResponse getAsyncResults(URI uri, MediaType mediaType, long waitTime, long numRetries) throws SodaError, InterruptedException
    {

        for (int i=0; i<numRetries; i++) {

            try {
                final ClientResponse response = httpLowLevel.follow202(uri, mediaType, waitTime);
                return response;
            } catch (LongRunningQueryException e) {
                uri = e.location;
                waitTime = Math.max((e.timeToRetry - System.currentTimeMillis()), 0);
            }
        }

        throw new SodaError("Long running result did not complete within the allotted time.");
    }

    /**
     * Method to check the async callbacks for new responses.
     *
     * @param uri the URI to go to for responses.
     * @param waitTime the time to wait until the first response
     * @param numRetries the total number of times to retry before failing.
     * @param cls the class of the object to return.
     * @return the object returned for a successful response.
     *
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    final public <T> T getAsyncResults(URI uri, long waitTime, long numRetries, Class<T> cls) throws SodaError, InterruptedException
    {

        final ClientResponse response = getAsyncResults(uri, HttpLowLevel.JSON_TYPE, waitTime, numRetries);
        return response.getEntity(cls);
    }

    /**
     * Method to check the async callbacks for new responses.
     *
     * @param uri the URI to go to for responses.
     * @param waitTime the time to wait until the first response
     * @param numRetries the total number of times to retry before failing.
     * @param cls the GenericType describing the class of the object to return.
     * @return the object returned for a successful response.
     *
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    final public <T> T getAsyncResults(URI uri, MediaType mediaType, long waitTime, long numRetries, GenericType<T> cls) throws SodaError, InterruptedException
    {
        final ClientResponse response = getAsyncResults(uri, mediaType, waitTime, numRetries);
        return response.getEntity(cls);
    }

    /**
     * The HttpLowLevel used for communicating with the service.
     * @return HttpLowLevel used for communicating with the service.
     */
    final public HttpLowLevel getHttpLowLevel()
    {
        return httpLowLevel;
    }
}
