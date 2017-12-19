package com.socrata.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Comment;
import com.socrata.model.GeocodingResults;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.requests.SodaRequest;
import javax.ws.rs.core.Response;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Implements the methods important to using the Soda workflow.  At a high level,
 * these are the APIs around publishing and determining if processes like geocoding
 * are still going on.
 *
 * To find out more about the publishing cycle, look at http://dev.socrata.com/publishers/workflow
 *
 * However, for frequent updates or large datasets, it is often better to us the Soda2Producer API if you can, because
 * publishing operations are expensive.
 */
public class SodaWorkflow
{
    protected static final String API_BASE_PATH         = "api";
    protected static final String GEO_BASE_PATH         = "geocoding";
    protected static final long   TICKET_CHECK          = 10000L;
    protected static final String VIEWS_BASE_PATH       = "views";
    protected static final String COMMENTS_METHOD_PATH  = "comments";

    protected final URI           geocodingUri;
    protected final HttpLowLevel  httpLowLevel;
    protected final URI           viewUri;
    protected final ObjectMapper  mapper;

    /**
     * Create a new SodaWorkflow object, using the supplied credentials for authentication.
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     *
     * @return fully configured SodaWorkflow
     */
    public static final SodaWorkflow newWorkflow(final String url, String userName, String password, String token)
    {
        return new SodaWorkflow(HttpLowLevel.instantiateBasic(url, userName, password, token, null));
    }

    /**
     * Constructor.
     *
     * @param httpLowLevel the HttpLowLevel this uses to contact the server
     */
    public SodaWorkflow(HttpLowLevel httpLowLevel)
    {
        this.httpLowLevel = httpLowLevel;

        geocodingUri = httpLowLevel.uriBuilder()
                                   .path(API_BASE_PATH)
                                   .path(GEO_BASE_PATH)
                                   .build();

        viewUri = httpLowLevel.uriBuilder()
                              .path(API_BASE_PATH)
                              .path(VIEWS_BASE_PATH)
                              .build();

        mapper = httpLowLevel.getObjectMapper();
    }

    /**
     * Gets the underlying connection.
     *
     * @return the underlying connection
     */
    public HttpLowLevel getHttpLowLevel()
    {
        return httpLowLevel;
    }


    /**
     * Publishes a dataset.
     *
     * @param datasetId id of the dataset to publish.
     * @return the view of the published dataset.
     *
     * @throws com.socrata.exceptions.SodaError
     * @throws InterruptedException
     */
    public DatasetInfo publish(final String datasetId) throws SodaError, InterruptedException
    {
        return publish(datasetId, false);
    }

    /**
     * Publishes a dataset.
     *
     * @param datasetId id of the dataset to publish.
     * @param async whether to force use of the ticketed/202 API response path. this can help with networks that don't
     *              like long-lived connections. this method call remains synchronous, and will block until the process
     *              returns. leaving off this parameter or setting false will revert the API to automatic determination
     *              of which response type to use.
     * @return the view of the published dataset.
     *
     * @throws com.socrata.exceptions.SodaError
     * @throws InterruptedException
     */
    public DatasetInfo publish(final String datasetId, final boolean async) throws SodaError, InterruptedException
    {


        waitForPendingGeocoding(datasetId);

        SodaRequest<String> requester = new SodaRequest<String>(datasetId,null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI publicationUri = UriBuilder.fromUri(viewUri)
                                                     .path(resourceId)
                                                     .path("publication")
                                                     .build();

                // TODO: use StringBuilder and URIEncoder. would throw IOException, so change when API breaks.
                final String querystring = "viewId=" + resourceId + "&async=" + Boolean.toString(async);
                return httpLowLevel.postRaw(publicationUri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, querystring);
            }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return response.readEntity(DatasetInfo.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            LongRunningRequest<String, DatasetInfo> longRunningRequest = new LongRunningRequest<>(e, DatasetInfo.class, requester);
            HttpLowLevel http = getHttpLowLevel();
            return longRunningRequest.checkStatus(http, http.getStatusCheckErrorRetries(), http.getStatusCheckErrorTime());
        }
    }

    /**
     * Creates a working copy for the specified dataset ID.  This copy can have any schema changes
     * made to it, as well as replace/append operations.
     *
     * The resulting dataset will not be available to other users until it is published.
     *
     * @param datasetId the id of the dataset.
     * @return reference to the new working copy of this dataset
     * @throws SodaError
     * @throws InterruptedException
     */
    public DatasetInfo createWorkingCopy(final String datasetId) throws SodaError, InterruptedException{

        SodaRequest<String> requester = new SodaRequest<String>(datasetId,null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI publicationUri = UriBuilder.fromUri(viewUri)
                                                     .path(resourceId)
                                                     .path("publication.json")
                                                     .queryParam("method", "copy")
                                                     .build();

                return httpLowLevel.postRaw(publicationUri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, "method=copy");
            }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return response.readEntity(Dataset.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            LongRunningRequest<String, DatasetInfo> longRunningRequest = new LongRunningRequest<>(e, DatasetInfo.class, requester);
            HttpLowLevel http = getHttpLowLevel();
            return longRunningRequest.checkStatus(http, http.getStatusCheckErrorRetries(), http.getStatusCheckErrorTime());
        }
    }

    /**
     * Makes a dataset public.  After calling this on a dataset, it will be visible to
     * any user, meaning any use will have teh "viewer" role for this dataset.
     *
     * @param datasetId id of the dataset to make public.
     */
    public void makePublic(final String datasetId) throws SodaError, InterruptedException {
        SodaRequest<String> requester = new SodaRequest<String>(datasetId,null)
        {

            //accessType=WEBSITE&method=setPermission&value=public.read
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI publicationUri = UriBuilder.fromUri(viewUri)
                                                     .path(resourceId)
                                                     .queryParam("accessType", "WEBSITE")
                                                     .queryParam("method", "setPermission")
                                                     .queryParam("value", "public.read")
                                                     .build();

                return httpLowLevel.putRaw(publicationUri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, "method=setPermission");
            }
        };

        try {
            requester.issueRequest().close();
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Dataset.class, requester);
        }
    }


    /**
     * Makes a dataset private, so it can only be viewed by users that it has been shared
     * with, or people who are admins on the site.
     *
     * @param datasetId id of the dataset
     */
    public void makePrivate(final String datasetId) throws SodaError, InterruptedException {
        SodaRequest<String> requester = new SodaRequest<String>(datasetId,null)
        {

            //accessType=WEBSITE&method=setPermission&value=public.read
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI publicationUri = UriBuilder.fromUri(viewUri)
                                                     .path(resourceId)
                                                     .queryParam("accessType", "WEBSITE")
                                                     .queryParam("method", "setPermission")
                                                     .queryParam("value", "private")
                                                     .build();

                return httpLowLevel.putRaw(publicationUri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, "method=setPermission");
            }
        };

        try {
            requester.issueRequest().close();
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Dataset.class, requester);
        }
    }

    /**
     * Waits for pending geocodes to be finished.  A publish won't work until all active geocoding requests are
     * handled.
     *
     * @param datasetId id of the dataset to check for outstanding geocodes.
     * @throws InterruptedException
     * @throws SodaError
     */
    public void waitForPendingGeocoding(final String datasetId) throws InterruptedException, SodaError
    {
        GeocodingResults geocodingResults = findPendingGeocodingResults(datasetId);
        while (geocodingResults.getView() > 0)
        {
            try { Thread.sleep(TICKET_CHECK); } catch (InterruptedException e) {}
            geocodingResults = findPendingGeocodingResults(datasetId);
        }
    }

    /**
     * Checks to see if the current dataset has any pending Geocoding results.
     *
     * @param datasetId id of the dataset
     * @return The Geocoding results for this dataset.
     *
     * @throws SodaError
     * @throws InterruptedException
     */
    public GeocodingResults findPendingGeocodingResults(final String datasetId) throws SodaError, InterruptedException
    {
        SodaRequest<String> requester = new SodaRequest<String>(datasetId, null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(geocodingUri)
                                          .path(datasetId)
                                          .queryParam("method", "pending")
                                          .build();

                return httpLowLevel.queryRaw(uri, HttpLowLevel.JSON_TYPE);
            }
        };


        try {
            final Response response = requester.issueRequest();
            try {
                return response.readEntity(GeocodingResults.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), GeocodingResults.class, requester);
        }
    }

    public Comment addComment(final String datasetId, final Comment comment) throws SodaError, InterruptedException
    {

        SodaRequest<String> requester = new SodaRequest<String>(datasetId, null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(viewUri)
                                          .path(datasetId)
                                          .path(COMMENTS_METHOD_PATH)
                                          .build();

                return httpLowLevel.postRaw(uri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, comment);
            }
        };


        try {
            final Response response = requester.issueRequest();
            try {
                 return response.readEntity(Comment.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Comment.class, requester);
        }
    }

}
