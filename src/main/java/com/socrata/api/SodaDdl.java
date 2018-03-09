package com.socrata.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Preconditions;
import com.socrata.api.DatasetDestination;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.SearchResults;
import com.socrata.model.importer.*;
import com.socrata.model.requests.SodaRequest;
import com.socrata.model.search.SearchClause;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Provides APIs for altering the shape of a dataset, whether that be adding columns,
 * removing columns, etc.
 */
public class SodaDdl extends SodaWorkflow
{
    protected static final String COLUMNS_PATH         = "columns";
    protected static final String ASSET_BASE_PATH      = "assets";
    protected static final String FILE_RESOURCE_PATH   = "file_data";
    protected static final String SEARCH_BASE_PATH     = "search";
    private final URI assetUri;
    private final URI fileResourceUri;
    private final URI searchUri;

    /**
     * Create a new SodaDdl object, using the supplied credentials for authentication.
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     *
     * @return fully configured SodaDdl
     */
    public static final SodaDdl newDdl(final String url, String userName, String password, String token)
    {
        return new SodaDdl(HttpLowLevel.instantiateBasic(url, userName, password, token, null));
    }

    /**
     * Constructor.
     *
     * @param httpLowLevel the HttpLowLevel this uses to contact the server
     */
    public SodaDdl(final HttpLowLevel httpLowLevel)
    {
        super(httpLowLevel);


        assetUri = httpLowLevel.uriBuilder()
                               .path(API_BASE_PATH)
                               .path(ASSET_BASE_PATH)
                               .build();

        searchUri = httpLowLevel.uriBuilder()
                                .path(API_BASE_PATH)
                                .path(SEARCH_BASE_PATH)
                                .path(VIEWS_BASE_PATH)
                                .build();

        fileResourceUri = httpLowLevel.uriBuilder()
                               .path(API_BASE_PATH)
                               .path(FILE_RESOURCE_PATH)
                               .build();



    }


    /**
     * Searches for matching datasets based on certain criteria.  The criteria to search on are based on
     * the SearchClauses classes.  Multiple clauses will be treated as AND-ed together.
     *
     * @param searchClauses the clauses to search
     * @return
     * @throws SodaError
     * @throws InterruptedException
     */
    public SearchResults searchViews(SearchClause... searchClauses) throws SodaError, InterruptedException
    {
        final UriBuilder searchBuilder = UriBuilder.fromUri(searchUri);
        for (SearchClause clause : searchClauses) {
            searchBuilder.queryParam(clause.getQueryParamName(), clause.getValue());
        }
        searchBuilder.queryParam("limit", 200);


        SodaRequest requester = new SodaRequest<URI>(null, searchBuilder.build())
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return httpLowLevel.queryRaw(payload, MediaType.APPLICATION_JSON_TYPE); }
        };

        try {
            final Response response = requester.issueRequest();
            try {
                return response.readEntity(SearchResults.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, MediaType.APPLICATION_JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<SearchResults>() {}, requester);
        }
    }

    /**
     * Creates an empty dataset, based on the dataset passed in.
     *
     * The new dataset will be unpublished.
     *
     * @param dataset dataset to create the new dataset on.  The ID should NOT be set.
     * @return the created dataset, the ID will be set on this.
     * @throws SodaError
     * @throws InterruptedException
     */
    public DatasetInfo createDataset(final DatasetInfo dataset) throws SodaError, InterruptedException
    {
        return createDataset(dataset, false);
    }

    /**
     * Creates an empty dataset, based on the dataset passed in.
     *
     * The new dataset will be unpublished.
     *
     * Prefer to use the createDataset(DatasetInfo, DatasetDestination) method, as this
     * one has surprising behavior when useNewBackend is false.
     *
     * @param dataset dataset to create the new dataset on.  The ID should NOT be set.
     * @param useNewBackend if true create dataset on the New Backend, otherwise use the default.
     * @return the created dataset, the ID will be set on this.
     * @throws SodaError
     * @throws InterruptedException
     */
    public DatasetInfo createDataset(final DatasetInfo dataset, final boolean useNewBackend) throws SodaError, InterruptedException
    {
        return createDataset(dataset, useNewBackend ? DatasetDestination.NBE : null);
    }

    /**
     * Creates an empty dataset, based on the dataset passed in.
     *
     * The new dataset will be unpublished.
     *
     * @param dataset dataset to create the new dataset on.  The ID should NOT be set.
     * @param destination Specify the backend for the new dataset, or null for the default.
     * @return the created dataset, the ID will be set on this.
     * @throws SodaError
     * @throws InterruptedException
     */
    public DatasetInfo createDataset(final DatasetInfo dataset, final DatasetDestination destination) throws SodaError, InterruptedException
    {
        SodaRequest requester = new SodaRequest<DatasetInfo>(null, dataset)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                httpLowLevel.setDatasetDestination(destination);
                return httpLowLevel.postRaw(viewUri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, payload);
            }
        };

        try {
            final Response response = requester.issueRequest();
            try {
                return response.readEntity(DatasetInfo.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), DatasetInfo.class, requester);
        }
    }

    /**
     * Loads a dataset or view based on it's ID
     *
     * @param id the ID to load the view through.
     * @return The View with the supplied ID.
     *
     * @throws InterruptedException
     * @throws SodaError
     */
    public DatasetInfo loadDatasetInfo(final String id) throws SodaError, InterruptedException
    {

        final URI uri = UriBuilder.fromUri(viewUri)
                                  .path(id)
                                  .build();

        SodaRequest requester = new SodaRequest<URI>(null, uri)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return httpLowLevel.queryRaw(payload, HttpLowLevel.JSON_TYPE); }
        };

        try {

            final Response response = httpLowLevel.queryRaw(uri, HttpLowLevel.JSON_TYPE);
            try {
                return response.readEntity(DatasetInfo.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), DatasetInfo.class, requester);
        }
    }


    /**
     * Updates a view.  Depending on the properties being updated, this MAY require the dataset to be
     * a working copy.
     *
     * @param datasetInfo the dataset to update to.  The ID MUST be set.  If this is a Dataset object, the
     *                    dataset should be a working copy.
     * @return the dataset after the update.
     *
     * @throws SodaError
     * @throws InterruptedException
     */
    public  DatasetInfo updateDatasetInfo(final DatasetInfo datasetInfo) throws SodaError, InterruptedException
    {


        SodaRequest requester = new SodaRequest<DatasetInfo>(datasetInfo.getId(), datasetInfo)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {   URI uri = UriBuilder.fromUri(viewUri)
                                    .path(resourceId)
                                    .build();
                return httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, payload);
            }
        };

        try {
            final Response response = requester.issueRequest();
            try {
                return response.readEntity(DatasetInfo.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), DatasetInfo.class, requester);
        }
    }

    /**
     * Deletes a dataset
     *
     * @param id the ID of the dataset to delete
     * @throws SodaError
     * @throws InterruptedException
     */
    public void deleteDataset(final String id) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<DatasetInfo>(id, null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {   URI uri = UriBuilder.fromUri(viewUri)
                                    .path(id)
                                    .build();
                return httpLowLevel.deleteRaw(uri);
            }
        };

        try {
            requester.issueRequest().close();
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Dataset.class, requester);
        }
    }


    /**
     * Adds a column to the dataset, and returns a definition of the dataset.
     *
     * @param datasetId id of the dataset to add the column to
     * @param column column definition
     * @return the added column
     * @throws SodaError
     * @throws InterruptedException
     */
    public Column addColumn(final String datasetId, final Column column) throws SodaError, InterruptedException
    {
        SodaRequest requester = new SodaRequest<Column>(datasetId, column)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(viewUri)
                                          .path(resourceId)
                                          .path(COLUMNS_PATH)
                                          .build();
                return httpLowLevel.postRaw(uri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, payload);
            }
        };

        try {
            final Response response = requester.issueRequest();
            try {
                return response.readEntity(Column.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Column.class, requester);
        }

    }

    /**
     * Removes a column to the dataset, and returns a definition of the dataset.
     *
     * @param datasetId id of the dataset to add the column to
     * @param columnId if of the column to delete
     * @throws LongRunningQueryException
     * @throws SodaError
     */
    public void removeColumn(final String datasetId, final int columnId) throws LongRunningQueryException, SodaError
    {

        final URI uri = UriBuilder.fromUri(viewUri)
                                  .path(datasetId)
                                  .path(COLUMNS_PATH)
                                  .path(Integer.toString(columnId))
                                  .build();

        httpLowLevel.deleteRaw(uri).close();
    }

    /**
     * Alters a column to the dataset, and returns a definition of the dataset.
     *
     * @param datasetId id of the dataset to change the column to
     * @param column column definition
     * @return the added column
     * @throws SodaError
     * @throws InterruptedException
     */
    public Column alterColumn(final String datasetId, final Column column) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<Column>(datasetId, column)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(viewUri)
                                          .path(resourceId)
                                          .path(COLUMNS_PATH)
                                          .path(Integer.toString(payload.getId()))
                                          .build();
                return httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, ContentEncoding.IDENTITY, payload);
            }
        };

        try {
            final Response response = requester.issueRequest();
            try {
                return response.readEntity(Column.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Column.class, requester);
        }
    }

    /**
     * Adds an asset to the Socrata Service.  An Asset is a file stored as a blob on the service.
     *
     * @param file file to upload
     * @return the asset ID and name
     */
    public AssetResponse addAsset(final File file) throws SodaError, InterruptedException, IOException
    {

        SodaRequest requester = new SodaRequest<File>(null, file)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                return httpLowLevel.postFileRaw(assetUri, MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_PLAIN_TYPE, payload);
            }
        };

        try {
            final Response response = requester.issueRequest();
            try {
                return mapper.readValue(response.readEntity(InputStream.class), AssetResponse.class);
            } finally {
                response.close();
            }
            //return response.getEntity(AssetResponse.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), AssetResponse.class, requester);
        } catch (JsonMappingException e) {
            throw new SodaError("Illegal response from the service.");
        } catch (JsonParseException e) {
            throw new SodaError("Invalid JSON returned from the service.");
        } catch (IOException e) {
            throw new SodaError("Error communicating with service.", e);
        }
    }

    /**
     * Get an asset given the ID.
     *
     * @param id id of the asset to load
     * @return InputStream of the file saved as the asset
     */
    public InputStream getAsset(final String id) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<File>(id, null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(assetUri)
                                          .path(resourceId)
                                          .build();

                return httpLowLevel.queryRaw(uri, MediaType.WILDCARD_TYPE);
            }
        };

        try {
            final Response response = requester.issueRequest();
            return response.readEntity(InputStream.class); // ...and how does the response get closed?
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), InputStream.class, requester);
        }
    }

    /**
     * Downloads a file blob, based on the NonDataFileDataset it's part of.
     * @return
     */
    public InputStream getFileBlob(final NonDataFileDataset dataset) throws SodaError, InterruptedException
    {
        Preconditions.checkArgument(dataset.getBlobId()!=null, "Dataset MUST be imported already before calling this.  Otherwise, the file doesn't have a Blob ID yet.");


        SodaRequest requester = new SodaRequest<File>(dataset.getBlobId(), null)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(fileResourceUri)
                                          .path(resourceId)
                                          .build();

                return httpLowLevel.queryRaw(uri, MediaType.WILDCARD_TYPE);
            }
        };

        try {
            final Response response = requester.issueRequest();
            return response.readEntity(InputStream.class); // ...and how does the response get closed?
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), InputStream.class, requester);
        }
    }

}
