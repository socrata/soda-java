package com.socrata.api;


import com.google.common.collect.ImmutableMap;
import com.socrata.exceptions.*;
import com.socrata.model.SodaErrorResponse;
import com.socrata.model.requests.SodaRequest;
import com.socrata.utils.JacksonObjectMapperProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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


    protected static final int DEFAULT_MAX_RETRIES = 200;
    public static final long   DEFAULT_RETRY_TIME = 4000;

    public static final String SODA_VERSION = "$$version";
    public static final String SOCRATA_TOKEN_HEADER = "X-App-Token";
    public static final String AUTH_REQUIRED_CODE = "authentication_required";
    public static final String UNEXPECTED_ERROR = "uexpectedError";

    public static final Map<String, String> UTF_PARAMS = ImmutableMap.of("charset", "UTF-8");
    public static final MediaType JSON_TYPE = MediaType.APPLICATION_JSON_TYPE;
    public static final MediaType CSV_TYPE = new MediaType("text", "csv");
    public static final MediaType UTF8_TEXT_TYPE = new MediaType("text", "plain", UTF_PARAMS);

    public static final GenericType<List<Object>> MAP_OBJECT_TYPE = new GenericType<List<Object>>() {};

    private final Client client;
    private final String url;

    private long retryTime = DEFAULT_RETRY_TIME;
    private long maxRetries = DEFAULT_MAX_RETRIES;

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
     * Gets the number of milliseconds to wait before following a 202
     * @return number of milliseconds to wait before following a 202
     */
    public long getRetryTime()
    {
        return retryTime;
    }

    /**
     * Sets the number of milliseconds to wait before following a 202
     * @param retryTime number of milliseconds to wait before following a 202
     */
    public void setRetryTime(long retryTime)
    {
        this.retryTime = retryTime;
    }

    /**
     * Gets the max number of times to follow a 202 before failing
     * @return max number of times to follow a 202 before failing
     */
    public long getMaxRetries()
    {
        return maxRetries;
    }

    /**
     * Sets the max number of times to follow a 202 before failing
     * @param maxRetries max number of times to follow a 202 before failing
     */
    public void setMaxRetries(long maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    public UriBuilder uriBuilder() {
        return UriBuilder.fromUri(url);
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
    public ClientResponse follow202(final URI uri, final MediaType mediaType, final long retryTime, final SodaRequest request2Rerun) throws InterruptedException, LongRunningQueryException, SodaError
    {
        if (retryTime > 0) {
            synchronized (this) {
                this.wait(retryTime);
            }
        }

        if (uri != null) {
            return queryRaw(uri, mediaType);
        } else {
            return request2Rerun.issueRequest();
        }
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
    final public <T> T getAsyncResults(URI uri, long waitTime, long numRetries, Class<T> cls, final SodaRequest request2Rerun) throws SodaError, InterruptedException
    {

        final ClientResponse response = getAsyncResults(uri, HttpLowLevel.JSON_TYPE, waitTime, numRetries, request2Rerun);
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
    final public <T> T getAsyncResults(URI uri, MediaType mediaType, long waitTime, long numRetries, GenericType<T> cls, SodaRequest request2Rerun) throws SodaError, InterruptedException
    {
        final ClientResponse response = getAsyncResults(uri, mediaType, waitTime, numRetries, request2Rerun);
        return response.getEntity(cls);
    }

    /**
     * Method to check the async callbacks for new responses.
     *
     * @param uri the URI to go to for responses.
     * @param waitTime the time to wait until the first response
     * @param numRetries the total number of times to retry before failing.
     * @param request2Rerun the object to use to re-run the request.
     * @return the ClientReponse for a successful response.
     *
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    final public ClientResponse getAsyncResults(URI uri, MediaType mediaType, long waitTime, long numRetries, SodaRequest request2Rerun) throws SodaError, InterruptedException
    {

        for (int i=0; i<numRetries; i++) {

            try {
                final ClientResponse response = follow202(uri, mediaType, waitTime, request2Rerun);
                return response;
            } catch (LongRunningQueryException e) {

                if (e.location != null) {
                    uri = e.location;
                }
            }
        }

        throw new SodaError("Long running result did not complete within the allotted time.");
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
    public ClientResponse deleteRaw(final URI uri) throws LongRunningQueryException, SodaError
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
     * @param acceptType the MIME Type accepted by this client
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public ClientResponse queryRaw(final URI uri, final MediaType acceptType) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                                                    .accept(acceptType);

        final ClientResponse response = builder.get(ClientResponse.class);
        return processErrors(response);
    }

    /**
     * Issues a raw POST to a URI.  The URI should be properly formed, and the response will process the errors
     * and throw if there are any.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @param mediaType the MIME type the object is to be sent to the server as.
     * @param object the object to send down to the server.  This can be a Jackson serializable object or a raw
     *               InputStream.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public ClientResponse postRaw(final URI uri, final MediaType mediaType, final Object object) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                .accept("application/json")
                .type(mediaType);

        final ClientResponse response = builder.post(ClientResponse.class, object);
        return processErrors(response);
    }

    public ClientResponse postFileRaw(final URI uri, final MediaType mediaType, final File file) throws LongRunningQueryException, SodaError {
        return postFileRaw(uri, mediaType, MediaType.APPLICATION_JSON_TYPE, file);
    }

    public ClientResponse postFileRaw(final URI uri, final MediaType mediaType, final MediaType acceptType, final File file) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                                                  .accept(acceptType)
                                                  .type(MediaType.MULTIPART_FORM_DATA_TYPE);

        FormDataMultiPart form = new FormDataMultiPart();
        form.bodyPart(new FileDataBodyPart(file.getName(), file, mediaType));

        final ClientResponse response = builder.post(ClientResponse.class, form);
        return processErrors(response);
    }

    /**
     * Issues a raw PUT to a URI.  The URI should be properly formed, and the response will process the errors
     * and throw if there are any.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @param mediaType the MIME type the object is to be sent to the server as.
     * @param object the object to send down to the server.  This can be a Jackson serializable object or a raw
     *               InputStream.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public <T> ClientResponse putRaw(final URI uri, final MediaType mediaType, final Object object) throws LongRunningQueryException, SodaError
    {
        final WebResource.Builder builder = client.resource(soda2ifyUri(uri))
                                                  .accept("application/json")
                                                  .type(mediaType);

        final ClientResponse response = builder.put(ClientResponse.class, object);
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

            String ticket = null;
            URI locationUri = null;


            //
            //  There are actually two ways Socrata currently deals with 202s, in the newer mechanism, they use
            //  the Location and Retry-After headers to direct where the "future" result is.  In the other mechanism,
            //  A specific "ticket" is created that needs to be combined with the original URL to get the "future"
            //  result.
            //
            if (StringUtils.isEmpty(location)) {

                final String body = response.getEntity(String.class);
                if (StringUtils.isEmpty(body)) {
                    throw new SodaError("Illegal body for 202 response.  No location and body is empty.");
                }

                try {
                    final ObjectMapper mapper = new ObjectMapper();
                    final Map<String, Object> bodyProperties = (Map<String, Object>)mapper.readValue(body, Object.class);
                    if (bodyProperties.get("ticket") != null) {
                        ticket = bodyProperties.get("ticket").toString();
                    }

                } catch (IOException ioe) {
                    throw new SodaError("Illegal body for 202 response.  No location or ticket.  Body = " + body);
                }

            } else {
                try {
                    locationUri = new URI(location);
                } catch (URISyntaxException e) {
                    throw new InvalidLocationError(location);
                }
            }

            throw new LongRunningQueryException(locationUri, parseRetryAfter(retryAfter), ticket);
        }

        if (!response.getType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            throw new SodaError(new SodaErrorResponse(UNEXPECTED_ERROR, response.getEntity(String.class), null, null));
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
        if (retryAfter == null) {
            return getRetryTime();
        }

        if (StringUtils.isNumeric(retryAfter)) {
            return System.currentTimeMillis() + Integer.parseInt(retryAfter) * 1000L;
        } else {
            try {
                final DateTime date = RFC1123_DATE_FORMAT.parseDateTime(retryAfter);
                if (date == null) {
                    return getRetryTime();
                }
                return date.getMillis();
            } catch (Exception e) {
                return getRetryTime();
            }

        }
    }


}
