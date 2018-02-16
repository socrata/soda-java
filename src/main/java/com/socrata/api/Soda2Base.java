package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Meta;
import com.socrata.model.UpsertResult;
import com.socrata.model.soql.SoqlQuery;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    public Response query(String resourceId, MediaType mediaType, SoqlQuery query) throws LongRunningQueryException, SodaError
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
     * @return The results from the operation
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public Response query(String resourceId, MediaType mediaType, String query) throws LongRunningQueryException, SodaError
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
    public Response getById(String resourceId,  MediaType mediaType, String uniqueId) throws LongRunningQueryException, SodaError
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
    public Response doTruncate(String resourceId) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);
        InputStream inputStream = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
        try {
            return httpLowLevel.putRaw(builder.build(), MediaType.APPLICATION_JSON_TYPE, httpLowLevel.getContentEncodingForUpserts(), inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) { /* ByteArrayInputStream does nothing and should never throw IOException */ }
        }
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
    public Response doDelete(String resourceId, String uniqueId) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId);
        return httpLowLevel.deleteRaw(builder.build());
    }


    /**
     * Adds a single row to a dataset.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param object The object that should be serialized to JSON and added to the dataset.  Jackson is used for serialization
     *               and deserialization.
     *
     * @return The results from the operation
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public <T> Response doAdd(String resourceId, T object) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        return httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, object);
    }

    /**
     * Adds a collection of rows to a dataset.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param objects The objects that should be serialized to JSON and added to the dataset.  Jackson is used for serialization
     *               and deserialization.
     *
     * @return The results from the operation
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public <T> Response doAddObjects(String resourceId, Collection<T> objects) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        return httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, objects);
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
     * @return The results from the operation
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public Response doAddStream(String resourceId, MediaType mediaType, InputStream stream) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        return httpLowLevel.postRaw(builder.build(), mediaType, httpLowLevel.getContentEncodingForUpserts(), stream);

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
     * @return The results from the operation
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public Response doUpdate(String resourceId, Object uniqueId, Object object) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId.toString());

        return httpLowLevel.postRaw(builder.build(), httpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, object);

    }


    public <T> Response doReplaceObjects(String resourceId, Collection<T> objects) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                               .path(SODA_BASE_PATH)
                                               .path(resourceId);

        return httpLowLevel.putRaw(builder.build(), httpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, objects);
    }

    public Response doReplaceStream(String resourceId, MediaType mediaType, InputStream stream) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = httpLowLevel.uriBuilder()
                                               .path(SODA_BASE_PATH)
                                               .path(resourceId);

        return httpLowLevel.putRaw(builder.build(), mediaType, httpLowLevel.getContentEncodingForUpserts(), stream);

    }

    /**
     * The HttpLowLevel used for communicating with the service.
     * @return HttpLowLevel used for communicating with the service.
     */
    final public HttpLowLevel getHttpLowLevel()
    {
        return httpLowLevel;
    }

    /**
     * Closes the connection, getting rid of any system reasources being held onto.
     */
    public void close() {
        httpLowLevel.close();
    }




}
