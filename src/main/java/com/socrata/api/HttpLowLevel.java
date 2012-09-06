package com.socrata.api;


import com.socrata.exceptions.*;
import com.socrata.model.UpsertResult;
import com.socrata.model.Meta;
import com.socrata.model.SodaErrorResponse;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.utils.JacksonObjectMapperProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;

/**
 * Class to handle all the low level HTTP operations. This class provides the core data access methods
 * for GET, PUT, POST, DELETE and wraps the appropriate authentications set-up for the connection.
 *
 * This library is based off the Jersey JAX-RS implementation.  Most of this is hidden from the
 * caller, but the Client object is available in case custom filters are required.
 */
public final class HttpLowLevel
{

    private static final DateTimeFormatter RFC1123_DATE_FORMAT = DateTimeFormat
            .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withLocale(Locale.US)
            .withZone(DateTimeZone.UTC);


    public static final long   DEFAULT_RETRY_TIME = 1000;
    public static final String SODA_BASE_PATH = "resource";
    public static final String SODA_QUERY = "$query";
    public static final String SODA_VERSION = "$$version";
    public static final String SOCRATA_TOKEN_HEADER = "X-App-Token";
    public static final String AUTH_REQUIRED_CODE = "authentication_required";

    private final Client client;
    private final String url;

    /**
     * Creates a client with the appropriate mappers and features turned on to
     * most easily map from SODA2 data types to Java data types.
     *
     * @return the Client that was created.
     */
    private static Client createClient() {
        final ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        clientConfig.getClasses().add(JacksonObjectMapperProvider.class);
        return Client.create(clientConfig);
    }

    /**
     * Create an anonymous connection to a SODA2 domain rooted at {@code url}
     *
     * @param url the base URL for the SODA2 domain to access.
     * @return HttpLowLevel object that is completely configured to use.
     */
    public static final HttpLowLevel instantiate(@Nonnull final String url)
    {
        return new HttpLowLevel(createClient(), url);
    }

    /**
     * Create an HttpLowLevel object that is set-up with the appropriate authentication credentials.
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     * @return HttpLowLevel object that is completely configured to use.
     */
    public static final HttpLowLevel instantiateBasic(@Nonnull final String url, @Nonnull final String userName, @Nonnull final String password, @Nullable final String token)
    {
        final Client client = createClient();
        client.addFilter(new HTTPBasicAuthFilter(userName, password));
        if (token != null) {
            client.addFilter(new SodaTokenFilter(token));
        }
        return new HttpLowLevel(client, url);
    }


    /**
     * Constructor
     *
     * @param client the Jersey Client class that will be used for actually issuing requests
     * @param url the base URL for the SODA2 domain to access.
     */
    public HttpLowLevel(Client client, final String url)
    {
        this.client = client;
        this.url = url;
    }

    /**
     * Returns the Jersey Client object this connection will use.
     *
     * @return Jersey Client object this connection will use.
     */
    public Client getClient()
    {
        return client;
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
    public ClientResponse query(String resourceId, SoqlQuery query) throws LongRunningQueryException, SodaError
    {
        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        return queryRaw(query.toSodaUri(builder).build());
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
    public ClientResponse query(String resourceId, String query) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .queryParam(SODA_QUERY, query);

        return queryRaw(builder.build());
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
    public ClientResponse getById(String resourceId, String uniqueId) throws LongRunningQueryException, SodaError
    {
        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId);
        return queryRaw(builder.build());
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
    public void truncate(String resourceId) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);
        deleteRaw(builder.build());
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
    public void delete(String resourceId, String uniqueId) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId);
        deleteRaw(builder.build());
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
    public <T> Meta add(String resourceId, T object) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = postRaw(builder.build(), object);
        return response.getEntity(Meta.class);
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
    public <T> UpsertResult addObjects(String resourceId, Collection<T> objects) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = postRaw(builder.build(), objects);
        return response.getEntity(UpsertResult.class);
    }

    /**
     * Adds a collection of rows to a dataset, but does so by simply streaming a datastream to the SODA2 server.  It is up
     * to the caller to make sure the stream is proper JSON.
     *
     * @param resourceId  The id of the resource to query.  This can either be the resource endpoint name
     *                    set in the metadata, or the unique ID given to the resource.
     * @param stream The objects to add, already serialized in a stream.
     *
     * @return The upsert result describing succeful and unsucessful operations.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public UpsertResult addStream(String resourceId, InputStream stream) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId);

        final ClientResponse response = postRaw(builder.build(), stream);
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
    public Meta update(String resourceId, Object uniqueId, Object object) throws LongRunningQueryException, SodaError
    {

        final UriBuilder builder = UriBuilder.fromUri(url)
                                             .path(SODA_BASE_PATH)
                                             .path(resourceId)
                                             .path(uniqueId.toString());

        final ClientResponse response = putRaw(builder.build(), object);
        return response.getEntity(Meta.class);
    }

    /**
     * Follows a 202 response that comes back for long running queries.
     *
     * @param uri the URI to go back to
     * @param retryTime the amount of time to wait for a retry
     * @return the ClientResponse from this operation
     *
     * @throws InterruptedException if this thread is interrupted
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public ClientResponse follow202(final URI uri, final long retryTime) throws InterruptedException, LongRunningQueryException, SodaError
    {
        final long timeToWait = System.currentTimeMillis() - retryTime;
        if (timeToWait > 0) {
            synchronized (this) {
                this.wait(timeToWait);
            }
        }

        return queryRaw(uri);
    }


    /**
     * Raw version of the API for issuing a delete, doing common error processing and returning the ClientResponse.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    protected ClientResponse deleteRaw(final URI uri) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                                                  .accept("application/json");

        final ClientResponse response = builder.delete(ClientResponse.class);
        return processErrors(response);
    }

    /**
     * Issues a raw GET to a URI.  The URI should be properly formed, and the response will process the errors
     * and throw if there are any.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    protected ClientResponse queryRaw(final URI uri) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                                                    .accept("application/json");

        final ClientResponse response = builder.get(ClientResponse.class);
        return processErrors(response);
    }

    /**
     * Issues a raw POST to a URI.  The URI should be properly formed, and the response will process the errors
     * and throw if there are any.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    protected <T> ClientResponse postRaw(final URI uri, final Object object) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                .accept("application/json")
                .type(MediaType.APPLICATION_JSON_TYPE);

        final ClientResponse response = builder.post(ClientResponse.class, object);
        return processErrors(response);
    }

    /**
     * Issues a raw PUT to a URI.  The URI should be properly formed, and the response will process the errors
     * and throw if there are any.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    protected <T> ClientResponse putRaw(final URI uri, final Object object) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                                                  .accept("application/json")
                                                  .type(MediaType.APPLICATION_JSON_TYPE);

        final ClientResponse response = builder.post(ClientResponse.class, object);
        return processErrors(response);
    }

    /**
     * Internal API to add any common parameters.  In this case, it sets the version parameter
     * so all our return types correspond to SODA2.
     *
     * @param uri URI to base the response on.
     * @return a new URI with the version parameter added.
     */
    private URI soda2ifyUri(final URI uri) {
        return UriBuilder.fromUri(uri).queryParam(SODA_VERSION, "2.0").build();
    }

    /**
     * Looks through a ClientResponse and throws any appropriate Java exceptions if there is an error.
     *
     * @param response ClientResponse to check for errors.
     * @return response that was passed in.
     *
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    private ClientResponse processErrors(final ClientResponse response) throws SodaError, LongRunningQueryException
    {

        if (response.getStatus() == 200) {
            return response;
        }

        if (response.getStatus() == 202) {
            final String location = response.getHeaders().getFirst("Location");
            final String retryAfter = response.getHeaders().getFirst("Retry-After");

            try {
                throw new LongRunningQueryException(new URI(location), parseRetryAfter(retryAfter));
            } catch (URISyntaxException e) {
                throw new InvalidLocationError(location);
            }
        }

        final SodaErrorResponse sodaErrorResponse = response.getEntity(SodaErrorResponse.class);
        if (response.getStatus() == 400) {
            throw new MalformedQueryError(sodaErrorResponse);
        } else if (response.getStatus() == 403) {
            if (AUTH_REQUIRED_CODE.equals(sodaErrorResponse.code)) {
                throw new MustBeLoggedInException(sodaErrorResponse);
            } else {
                throw new QueryTooComplexException(sodaErrorResponse);
            }
        } else if (response.getStatus() == 404){
            throw new DoesNotExistException(sodaErrorResponse);
        } else if (response.getStatus() == 408) {
            throw new QueryTimeoutException(sodaErrorResponse);
        }

        else throw new SodaError(sodaErrorResponse);
    }


    /**
     * Parses the RetryAfter dates to determine when to respond to a 202
     *
     * @param retryAfter the string returned from a 202 RetryAfter header
     * @return The time in milliseconds the caller should retry.  This is the time in milliseconds since the epoch,
     * NOT the number of milliseconds to wait.  To get milliseconds to wait, subtract current time.
     */
    private long parseRetryAfter(final String retryAfter) {
        if (StringUtils.isNumeric(retryAfter)) {
            return System.currentTimeMillis() + Integer.parseInt(retryAfter) * 1000L;
        } else {
            try {
                final DateTime date = RFC1123_DATE_FORMAT.parseDateTime(retryAfter);
                if (date == null) {
                    return DEFAULT_RETRY_TIME;
                }
                return date.getMillis();
            } catch (Exception e) {
                return DEFAULT_RETRY_TIME;
            }

        }
    }


}
