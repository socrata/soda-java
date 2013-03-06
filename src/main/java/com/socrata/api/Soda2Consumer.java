package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.requests.SodaRequest;
import com.socrata.model.soql.SoqlQuery;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import java.util.List;

/**
 * Class to issue SODA2 queries against a service.  All the methods on this class are READ-ONLY, and are only for consumption.
 *
 * For updating data using SODA2, use the Soda2Producer class.
 */
public class Soda2Consumer extends Soda2Base
{
    public static final GenericType<List<Object>> HASH_RETURN_TYPE = new GenericType(Object.class) {};

    /**
     * Creates a new Soda2Consumer that had no authentication associated with it.  All requests
     * will be done as an anonymous user.
     *
     * @param url the URL to the base SODA2 domain.
     * @return the new Soda2Consumer that has been fully configured.
     */
    public static final Soda2Consumer newConsumer(final String url)
    {
        return new Soda2Consumer(HttpLowLevel.instantiate(url));
    }

    /**
     * Creates a new Soda2Consumer with the passed in credentials associated with it.  All requests
     * will contain these credentials.
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     *
     * @return the new Soda2Consumer that has been fully configured.
     */
    public static final Soda2Consumer newConsumer(final String url, String userName, String password, String token)
    {
        return new Soda2Consumer(HttpLowLevel.instantiateBasic(url, userName, password, token));
    }

    /**
     * Constructor
     *
     * @param httpLowLevel the lower level connection object for using for requests.
     */
    public Soda2Consumer(HttpLowLevel httpLowLevel)
    {
        super(httpLowLevel);
    }

    /**
     * Retrieves an object on a resource based on it's ID.  If the object does not exist, a DoesNotExistException will
     * be thrown.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param id Id based on a dataset specific unique column, or the system ID created for each row.
     * @param cls The class object the resulting object should be deserialized as.
     * @return the object retrieved.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> T getById(String resourceId, String id, Class<T> cls) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<String>(resourceId, id)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return getById(resourceId, HttpLowLevel.JSON_TYPE, payload); }
        };

        try {
            final ClientResponse    response = requester.issueRequest();
            return response.getEntity(new GenericType<T>(cls));
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<T>(cls), requester);
        }
    }


    /**
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param query The query to be executed against the resource.
     * @param genericType the type of objects that should be returned as a result from this query
     * @param <T> the type of object that should be returned in the resulting list
     *
     * @return results from the query.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> List<T> query(String resourceId, SoqlQuery query, GenericType<List<T>> genericType) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<SoqlQuery>(resourceId, query)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return query(resourceId, HttpLowLevel.JSON_TYPE, payload); }
        };

        try {
            final ClientResponse    response = query(resourceId, HttpLowLevel.JSON_TYPE, query);
            return response.getEntity(genericType);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), genericType, requester);
        }


    }

    /**
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param query The query string to be executed against the resource.
     * @param genericType the type of objects that should be returned as a result from this query
     * @param <T> the type of object that should be returned in the resulting list
     *
     * @return results from the query.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> List<T>  query(String resourceId, String query, GenericType<List<T>> genericType) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<String>(resourceId, query)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return query(resourceId, HttpLowLevel.JSON_TYPE, payload); }
        };

        try {
            final ClientResponse    response = requester.issueRequest();
            return response.getEntity(genericType);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), genericType, requester);
        }
    }


}
