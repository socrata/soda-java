package com.socrata.api;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.socrata.exceptions.*;
import com.socrata.model.SodaErrorResponse;
import com.socrata.model.requests.SodaRequest;
import com.socrata.utils.JacksonObjectMapperProvider;
import com.socrata.utils.ObjectMapperFactory;
import com.socrata.utils.streams.CompressingGzipInputStream;
import org.glassfish.jersey.client.JerseyClient;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.client.Entity;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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

    private static final TypeReference<Map<String,Object>> GENERIC_JSON_OBJECT_TYPE = new TypeReference<Map<String, Object>>() {};


    protected static final int DEFAULT_MAX_RETRIES = 200;
    public static final long   DEFAULT_RETRY_TIME = 4000;

    protected static final int DEFAULT_STATUS_CHECK_ERROR_RETRIES = 5;
    protected static final long DEFAULT_STATUS_CHECK_ERROR_TIME = 4000;

    public static final String SODA_VERSION = "$$version";
    public static final String NBE_FLAG = "nbe";
    public static final String SOCRATA_TOKEN_HEADER = "X-App-Token";
    public static final String SOCRATA_REQUEST_ID_HEADER = "X-Socrata-RequestId";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String AUTH_REQUIRED_CODE = "authentication_required";
    public static final String UNEXPECTED_ERROR = "uexpectedError";
    public static final String MALFORMED_RESPONSE = "malformedResponse";

    public static final Map<String, String> UTF_PARAMS = ImmutableMap.of("charset", "UTF-8");
    public static final MediaType JSON_TYPE = MediaType.APPLICATION_JSON_TYPE;
    public static final MediaType CSV_TYPE = new MediaType("text", "csv");
    public static final MediaType UTF8_TEXT_TYPE = new MediaType("text", "plain", UTF_PARAMS);

    public static final GenericType<List<Object>> MAP_OBJECT_TYPE = new GenericType<List<Object>>() {};

    private final ObjectMapper mapper;

    private final JerseyClient client;
    private final String url;

    private long retryTime = DEFAULT_RETRY_TIME;
    private long maxRetries = DEFAULT_MAX_RETRIES;
    private ContentEncoding contentEncodingForUpserts = ContentEncoding.IDENTITY;

    private final ConcurrentHashMap<String, String> additionalParams = new ConcurrentHashMap<String, String>();

    private int statusCheckErrorRetries = DEFAULT_STATUS_CHECK_ERROR_RETRIES;
    private long statusCheckErrorTime = DEFAULT_STATUS_CHECK_ERROR_TIME;

    /**
     * Creates a client with the appropriate mappers and features turned on to
     * most easily map from SODA2 data types to Java data types.
     *
     * This call will honor {@code https.proxyHost} and {@code https.proxyPort} for setting a proxy.
     *
     * @return the Client that was created.
     */
    private static JerseyClient createClient() {

        String  proxyHost = System.getProperty("https.proxyHost");
        Integer proxyPort = null;
        if (StringUtils.isNotEmpty(proxyHost)) {
            final String proxyPortString = System.getProperty("https.proxyPort");
            if (StringUtils.isNotEmpty(proxyPortString)) {
                proxyPort = Integer.decode(proxyPortString);
            }
        }

        return createClient(proxyHost, proxyPort);
    }

    /**
     * Creates a client with the appropriate mappers and features turned on to
     * most easily map from SODA2 data types to Java data types.
     *
     * @param proxyHost the host to use a proxy.  If {@code null}, this will not use a proxy.
     * @param proxyPort the port to use for the proxy host.  If {@code null}, this will use the default HTTP port.
     *
     * @return the Client that was created.
     */
    private static JerseyClient createClient(@Nullable final String proxyHost, @Nullable final Integer proxyPort) {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(JacksonObjectMapperProvider.class);
        clientConfig.register(JacksonFeature.class);
        clientConfig.register(MultiPartFeature.class);

        if (StringUtils.isNotEmpty(proxyHost)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort == null ? 443 : proxyPort));
            HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
            cp.connectionFactory(new ProxyHandler(proxy));
            clientConfig.connectorProvider(cp);
        }
        return new JerseyClientBuilder().withConfig(clientConfig).build();
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
    public static final HttpLowLevel instantiateBasic(@Nonnull final String url, @Nonnull final String userName, @Nonnull final String password, @Nullable final String token, @Nullable final String requestId)
    {
        final JerseyClient client = createClient();
        client.register(HttpAuthenticationFeature.basic(userName, password));
        if (token != null) {
            client.register(new SodaTokenFilter(token));
        }
        if (requestId != null) {
            client.register(new SodaRequestIdFilter(requestId));
        }
        client.register(new UserAgentFilter());

        // I think this isn't necessary with jersey 2; in any event it's not done this way.
        //client.setChunkedEncodingSize(10240); // enable streaming and not put whole inputstream in memory

        //client.setConnectTimeout(1000 * 60);
        return new HttpLowLevel(client, url);
    }

    public HttpLowLevel(final JerseyClient client, final String url) {
        this(client, url, ObjectMapperFactory.create());
    }


    /**
     * Constructor
     *
     * @param client the Jersey Client class that will be used for actually issuing requests
     * @param url the base URL for the SODA2 domain to access.
     */
    public HttpLowLevel(final JerseyClient client, final String url, final ObjectMapper mapper)
    {
        this.client = client;
        this.url = url;
        this.mapper = mapper;
    }

    /**
     * Returns the Jersey Client object this connection will use.
     *
     * @return Jersey Client object this connection will use.
     */
    public JerseyClient getClient()
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

    /**
     * Get the number of status check retries on non-soda errors between long running request submission and completion
     * @return the number of status check retries on non-soda errors between long running request submission and completion
     */
    public int getStatusCheckErrorRetries()
    {
        return statusCheckErrorRetries;
    }

    /**
     * Set the number of status check retries on non-soda errors between long running request submission and completion
     * @param statusCheckErrorRetries number of status check retries on non-soda errors between long running request submission and completion
     */
    public void setStatusCheckErrorRetries(int statusCheckErrorRetries)
    {
        this.statusCheckErrorRetries = statusCheckErrorRetries;
    }

    /**
     * Get the status check interval time in ms on non-soda errors between long running request submission and completion
     * @return the status check interval time in ms on non-soda errors between long running request submission and completion
     */
    public long getStatusCheckErrorTime()
    {
        return statusCheckErrorTime;
    }

    /**
     * Set the status check interval time in ms on non-soda errors between long running request submission and completion
     * @param statusCheckErrorTime status check interval time in ms on non-soda errors between long running request submission and completion
     */
    public void setStatusCheckErrorTime(int statusCheckErrorTime)
    {
        this.statusCheckErrorTime = statusCheckErrorTime;
    }

    /**
     * Gets the content encoding for upserts.  This defaults to GZIP, which basically
     * means uncompressed streams will be gzipped before being sent up to the Socrata Service
     *
     * @return content encoding of the upserts.  If this is Identity, no encodings will be added.
     */
    public ContentEncoding getContentEncodingForUpserts()
    {
        return contentEncodingForUpserts;
    }


    /**
     * Sets the content encoding for upserts.  This defaults to GZIP, which basically
     * means uncompressed streams will be gzipped before being sent up to the Socrata Service
     *
     * @param contentEncodingForUpserts content encoding of the upserts.  If this is Identity, no encodings will be added.
     */
    public void setContentEncodingForUpserts(ContentEncoding contentEncodingForUpserts)
    {
        this.contentEncodingForUpserts = contentEncodingForUpserts;
    }

    /**
     * Get the map of additional parameters for this HttpLowLevel.  These parameters
     * will be added to every request.  The map returned will be thread safe for modifications.
     *
     * @return map of additional parameters
     */
    public Map<String, String> getAdditionalParameters() {
        return this.additionalParams;
    }

    public UriBuilder uriBuilder() {
        return UriBuilder.fromUri(url);
    }

    /**
     * Follows a 202 response that comes back for long running queries.
     *
     * @param uri the URI to go back to
     * @param retryTime the amount of time to wait for a retry
     * @return the Response from this operation
     *
     * @throws InterruptedException if this thread is interrupted
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public Response follow202(final URI uri, final MediaType mediaType, final long retryTime, final SodaRequest request2Rerun) throws InterruptedException, LongRunningQueryException, SodaError
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
        Response response = getAsyncResults(uri, HttpLowLevel.JSON_TYPE, waitTime, numRetries, request2Rerun);
        try {
            return response.readEntity(cls);
        } finally {
            response.close();
        }
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
        final Response response = getAsyncResults(uri, mediaType, waitTime, numRetries, request2Rerun);
        try {
             return response.readEntity(cls);
        } finally {
            response.close();
        }
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
    final public Response getAsyncResults(URI uri, MediaType mediaType, long waitTime, long numRetries, SodaRequest request2Rerun) throws SodaError, InterruptedException
    {

        for (int i=0; i<numRetries; i++) {

            try {
                return follow202(uri, mediaType, waitTime, request2Rerun);
            } catch (LongRunningQueryException e) {
                if (e.location != null) {
                    uri = e.location;
                }
            }
        }

        throw new SodaError("Long running result did not complete within the allotted time.");
    }



    /**
     * Raw version of the API for issuing a delete, doing common error processing and returning the Response.
     *
     * @param uri URI to issue a request to.  Any id information should have already been added.
     * @return the raw ClientReponse to the request.  Any errors will have already been processed, and have thrown
     * and exception.
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    public Response deleteRaw(final URI uri) throws LongRunningQueryException, SodaError
    {
        final Response response = client.target(soda2ifyUri(uri)).request().
            accept("application/json").
            delete(Response.class);

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
    public Response queryRaw(final URI uri, final MediaType acceptType) throws LongRunningQueryException, SodaError
    {
        final Response response = client.target(soda2ifyUri(uri)).request().
            accept(acceptType).
            get(Response.class);

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
    public Response postRaw(final URI uri, final MediaType mediaType, final ContentEncoding contentEncoding, Object object) throws LongRunningQueryException, SodaError
    {
        final JerseyInvocation.Builder builder = client.target(soda2ifyUri(uri)).request().
            accept("application/json");
        final Object encodedObject = encodeContents(contentEncoding, builder, object);
        final Response response = builder.post(Entity.entity(encodedObject, mediaType), Response.class);

        return processErrors(response);
    }

    /**
     * If true adds ?nbe=true flag to all URIs (to enable creating datasets on New Backend)
     *
     * Prefer to use the setDatasetDestination method, as this one has surprising behavior
     * when its parameter is false.
     *
     * @param useNbe if true use New Backend, otherwise use the default
     */
    public void setUseNewBackend(boolean useNbe) {
        if(useNbe) setDatasetDestination(DatasetDestination.NBE);
        else setDatasetDestination(null);
    }

    /**
     * Specify (or reset to the default) the backend on which datasets will be created.
     *
     * @param destination The target backend, or null for the default.
     */
    public void setDatasetDestination(DatasetDestination destination) {
        if(destination == null) additionalParams.remove(NBE_FLAG);
        else additionalParams.put(NBE_FLAG, Boolean.toString(destination == DatasetDestination.NBE));
    }

    private Object encodeContents(final ContentEncoding contentEncoding,
                                  final JerseyInvocation.Builder builder,
                                  final Object object) throws BadCompressionException
    {

        switch (contentEncoding) {
            case GZIP: {
                builder.header(HttpHeaders.CONTENT_ENCODING, contentEncoding.header);

                if (!(object instanceof InputStream)) {
                    throw new IllegalArgumentException("Can only compress puts that use an InputStream");
                }

                try {
                    return new CompressingGzipInputStream((InputStream)object) ;
                } catch (IOException ioe) {
                    throw new BadCompressionException(ioe);
                }
            }

            case IDENTITY: {
                return object;
            }

            default: {
                throw new IllegalArgumentException("Unknown ContentEncoding");
            }
        }
    }


    public Response postFileRaw(final URI uri, final MediaType mediaType, final File file) throws LongRunningQueryException, SodaError {
        return postFileRaw(uri, mediaType, MediaType.APPLICATION_JSON_TYPE, file);
    }

    public Response postFileRaw(final URI uri, final MediaType mediaType, final MediaType acceptType, File file) throws LongRunningQueryException, SodaError
    {
        final Response response;

        try(FormDataMultiPart form = new FormDataMultiPart()) {
            form.bodyPart(new FileDataBodyPart(file.getName(), file, mediaType));
            response = client.target(soda2ifyUri(uri)).request().
                accept(acceptType).
                post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA_TYPE), Response.class);
        } catch (IOException e) {
            throw new SodaError("IO error", e);
        }

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
    public <T> Response putRaw(final URI uri, final MediaType mediaType, final ContentEncoding contentEncoding, final Object object) throws LongRunningQueryException, SodaError
    {
        final JerseyInvocation.Builder builder = client.target(soda2ifyUri(uri)).request().
            accept("application/json");

        final Object encodedObject = encodeContents(contentEncoding, builder, object);
        final Response response = builder.put(Entity.entity(encodedObject, mediaType), Response.class);
        return processErrors(response);
    }


    public Response putFileRaw(final URI uri, final MediaType mediaType, final File file) throws LongRunningQueryException, SodaError {
        return putFileRaw(uri, mediaType, MediaType.APPLICATION_JSON_TYPE, file);
    }

    public Response putFileRaw(final URI uri, final MediaType mediaType, final MediaType acceptType, final File file) throws LongRunningQueryException, SodaError
    {
        final Response response;

        try(FormDataMultiPart form = new FormDataMultiPart()) {
            form.bodyPart(new FileDataBodyPart(file.getName(), file, mediaType));
            response = client.target(soda2ifyUri(uri)).request().
                accept(acceptType).
                put(Entity.entity(form, MediaType.MULTIPART_FORM_DATA_TYPE), Response.class);
        } catch (IOException e) {
            throw new SodaError("IO error", e);
        }

        return processErrors(response);
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * make the {@link ObjectMapper} available to other classes in this package so that they can
     * share the same configured instance instead of potentially using differently configured ObjectMappers
     */
    ObjectMapper getObjectMapper() {
        return mapper;
    }


    /**
     * Internal API to add any common parameters.  In this case, it sets the version parameter
     * so all our return types correspond to SODA2.
     *
     * @param uri URI to base the response on.
     * @return a new URI with the version parameter added.
     */
    private URI soda2ifyUri(final URI uri) {

        final UriBuilder    builder = UriBuilder.fromUri(uri).queryParam(SODA_VERSION, "2.0");

        for (String key : additionalParams.keySet()) {
            builder.queryParam(key, additionalParams.get(key));
        }

        return builder.build();
    }

    /**
     * Looks through a Response and throws any appropriate Java exceptions if there is an error.
     *
     * @param response Response to check for errors.
     * @return response that was passed in.
     *
     * @throws LongRunningQueryException thrown if this query is long running and a 202 is returned.  In this case,
     * the caller likely wants to call follow202.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     */
    private Response processErrors(final Response response) throws SodaError, LongRunningQueryException
    {
        int status = response.getStatus();
        if (status == 200 || status == 201 || status == 204) {
            return response;
        }

        try {
            final String body = response.readEntity(String.class);

            if (status == 202) {
                final String location = response.getStringHeaders().getFirst("Location");
                final String retryAfter = response.getStringHeaders().getFirst("Retry-After");

                String ticket = null;
                URI locationUri = null;


                //
                //  There are actually two ways Socrata currently deals with 202s, in the newer mechanism, they use
                //  the Location and Retry-After headers to direct where the "future" result is.  In the other mechanism,
                //  A specific "ticket" is created that needs to be combined with the original URL to get the "future"
                //  result.
                //
                if (StringUtils.isEmpty(location)) {

                    if (StringUtils.isEmpty(body)) {
                        throw new SodaError("Illegal body for 202 response.  No location and body is empty.");
                    }

                    try {
                        final Map<String, Object> bodyProperties = mapper.readValue(body, GENERIC_JSON_OBJECT_TYPE);
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

            if (response.getMediaType() != null && !response.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new SodaError(new SodaErrorResponse(UNEXPECTED_ERROR, body, null, null), status);
            }

            SodaErrorResponse sodaErrorResponse;
            if(body.isEmpty()) {
                sodaErrorResponse = new SodaErrorResponse(String.valueOf(status), null, null, null);
            } else {
                try {
                    sodaErrorResponse = mapper.readValue(body, SodaErrorResponse.class);
                } catch (Exception e) {
                    throw new SodaError(new SodaErrorResponse(MALFORMED_RESPONSE, body, null, null), status);
                }
            }

            switch (status) {
            case 400:
                if (sodaErrorResponse.message != null &&
                    sodaErrorResponse.message.startsWith("Row data was saved.")) {
                    throw new MetadataUpdateError(sodaErrorResponse);
                }
                throw new MalformedQueryError(sodaErrorResponse);
            case 403:
                if (AUTH_REQUIRED_CODE.equals(sodaErrorResponse.code)) {
                    throw new MustBeLoggedInException(sodaErrorResponse);
                } else {
                    throw new QueryTooComplexException(sodaErrorResponse);
                }
            case 404:
                throw new DoesNotExistException(sodaErrorResponse);
            case 408:
                throw new QueryTimeoutException(sodaErrorResponse);
            case 409:
                throw new ConflictOperationException(sodaErrorResponse);
            default:
                throw new SodaError(sodaErrorResponse, status);
            }
        } finally {
            response.close();
        }
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

    /**
     * An internal class we use for setting the proxy for an Http connection.
     */
    private static class ProxyHandler implements HttpUrlConnectorProvider.ConnectionFactory
    {

        final Proxy proxy;

        public ProxyHandler(@Nonnull Proxy proxy)
        {
            this.proxy = proxy;
        }

        @Override
        public HttpURLConnection getConnection(URL url) throws IOException
        {
            return (HttpURLConnection)url.openConnection(proxy);
        }
    }



}
