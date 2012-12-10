package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Meta;
import com.socrata.model.UpsertResult;
import com.socrata.model.soql.SoqlQuery;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * A base class that wraps some common SODA 2 behaviours around an HttpLowLevel object.
 *
 */
public class Soda2Base
{

    public static final String SODA_BASE_PATH = "resource";
    public static final String SODA_QUERY = "$query";

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
     * Runs a query against a SODA2 resource.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param query The query to be executed against the resource.
     *
     * @return a response containing the response stream, if the request is successful
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public ClientResponse query(String resourceId, MediaType mediaType, SoqlQuery query) throws LongRunningQueryException, SodaError
    {
        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        return httpLowLevel.queryRaw(query.toSodaUri(builder).build(), mediaType);
    }

    /**
     * Runs a query against a SODA2 resource.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param query The query string to be executed against the resource.  This should NOT be URL encoded.
     *
     * @return a response containing the response stream, if the request is successful.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public ClientResponse query(String resourceId, MediaType mediaType, String query) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .queryParam(SODA_QUERY, query);

        return httpLowLevel.queryRaw(builder.build(), mediaType);
    }

    /**
     * Load a single object based on it's unique ID.  This id can either be based on a dataset specific unique column,
     * or the system ID created for each row.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param uniqueId Id based on a dataset specific unique column, or the system ID created for each row.
     *
     * @return a response containing the response stream, if the request is successful.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public ClientResponse getById(String resourceId,  MediaType mediaType, String uniqueId) throws LongRunningQueryException, SodaError
    {
        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId);
        return httpLowLevel.queryRaw(builder.build(), mediaType);
    }

    /**
     * Truncates a dataset by removing all the rows in it.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public void doTruncate(String resourceId) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);
        httpLowLevel.deleteRaw(builder.build());
    }

    /**
     * Deletes a single row from a dataset.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param uniqueId Id based on a dataset specific unique column, or the system ID created for each row.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public void doDelete(String resourceId, String uniqueId) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId);
        httpLowLevel.deleteRaw(builder.build());
    }


    /**
     * Adds a single row to a dataset.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param object The object that should be serialized to JSON and added to the dataset.  Jackson is used for serialization
     *               and deserialization.
     *
     * @return the metadata for the object just added.  This will include the unique ID for the row, so it can be
     * unambigously loaded through getById
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public <T> Meta doAdd(String resourceId, T object) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, object);
        return response.getEntity(Meta.class);
    }

    /**
     * Adds a single row to a dataset.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param object The object that should be serialized to JSON and added to the dataset.  Jackson is used for serialization
     *               and deserialization.
     * @param retType type that should be returned from here.
     *
     * @return the metadata for the object just added.  This will include the unique ID for the row, so it can be
     * unambigously loaded through getById
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public <T> T doAdd(String resourceId, T object, Class<T> retType) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, object);
        return response.getEntity(retType);
    }

    /**
     * Adds a collection of rows to a dataset.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param objects The objects that should be serialized to JSON and added to the dataset.  Jackson is used for serialization
     *               and deserialization.
     *
     * @return The upsert result describing succeful and unsucessful operations.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public <T> UpsertResult doAddObjects(String resourceId, Collection<T> objects) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, objects);
        return response.getEntity(UpsertResult.class);
    }

    /**
     * Adds a collection of rows to a dataset, but does so by simply streaming a datastream to the SODA2 server.  Whether
     * the stream is JSON or CSV is set by the mediaType
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param mediaType The media type for the stream (normally JSON or CSV)
     * @param stream The objects to add, already serialized in a stream.
     *
     * @return The upsert result describing succeful and unsucessful operations.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public UpsertResult doAddStream(String resourceId, MediaType mediaType, InputStream stream) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = httpLowLevel.postRaw(builder.build(), mediaType, stream);
        return response.getEntity(UpsertResult.class);
    }



    /**
     * Update an object.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param uniqueId Id based on a dataset specific unique column, or the system ID created for each row.
     * @param object The object that should be serialized to JSON and added to the dataset.  Jackson is used for serialization
     *               and deserialization.
     *
     * @return the metadata for the object just added.  This will include the unique ID for the row, so it can be
     * unambigously loaded through getById
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public Meta doUpdate(String resourceId, Object uniqueId, Object object) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId.toString());

        final ClientResponse response = httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, object);
        return response.getEntity(Meta.class);
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
