package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.SearchResults;
import com.socrata.model.importer.AssetResponse;
import com.socrata.model.importer.Column;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.requests.SodaRequest;
import com.socrata.model.search.SearchClause;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

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
    protected static final String SEARCH_BASE_PATH     = "search";
    private final URI assetUri;
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
        return new SodaDdl(HttpLowLevel.instantiateBasic(url, userName, password, token));
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
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return httpLowLevel.queryRaw(payload, MediaType.APPLICATION_JSON_TYPE); }
        };

        try {
            final ClientResponse    response = requester.issueRequest();
            return response.getEntity(SearchResults.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, MediaType.APPLICATION_JSON_TYPE, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, new GenericType<SearchResults>() {}, requester);
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
        SodaRequest requester = new SodaRequest<DatasetInfo>(null, dataset)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return httpLowLevel.postRaw(viewUri, HttpLowLevel.JSON_TYPE, payload); }
        };

        try {
            final ClientResponse response = requester.issueRequest();
            return response.getEntity(DatasetInfo.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, DatasetInfo.class, requester);
        }
    }


    /**
     * Loads a dataset or view based on it's ID
     *
     * @param id the ID to load the view through.
     * @return The View with the supplied ID.
     *
     * @throws LongRunningQueryException
     * @throws SodaError
     */
    public DatasetInfo loadDatasetInfo(final String id) throws SodaError, InterruptedException
    {

        final URI uri = UriBuilder.fromUri(viewUri)
                                  .path(id)
                                  .build();

        SodaRequest requester = new SodaRequest<URI>(null, uri)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return httpLowLevel.queryRaw(payload, HttpLowLevel.JSON_TYPE); }
        };

        try {

            final ClientResponse response = httpLowLevel.queryRaw(uri, HttpLowLevel.JSON_TYPE);
            return response.getEntity(DatasetInfo.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, DatasetInfo.class, requester);
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
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {   URI uri = UriBuilder.fromUri(viewUri)
                                    .path(resourceId)
                                    .build();
                return httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, payload);
            }
        };

        try {

            final ClientResponse response = requester.issueRequest();
            return response.getEntity(DatasetInfo.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, DatasetInfo.class, requester);
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
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {   URI uri = UriBuilder.fromUri(viewUri)
                                    .path(id)
                                    .build();
                return httpLowLevel.deleteRaw(uri);
            }
        };

        try {
            requester.issueRequest();
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Dataset.class, requester);
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
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(viewUri)
                                          .path(resourceId)
                                          .path(COLUMNS_PATH)
                                          .build();
                return httpLowLevel.postRaw(uri, HttpLowLevel.JSON_TYPE, payload);
            }
        };

        try {
            final ClientResponse response = requester.issueRequest();
            return response.getEntity(Column.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Column.class, requester);
        }

    }

    /**
     * Removes a column to the dataset, and returns a definition of the dataset.
     *
     * @param datasetId id of the dataset to add the column to
     * @param columnId if of the column to delete
     * @return the added column
     * @throws SodaError
     * @throws InterruptedException
     */
    public void removeColumn(final String datasetId, final int columnId) throws LongRunningQueryException, SodaError
    {

        final URI uri = UriBuilder.fromUri(viewUri)
                                  .path(datasetId)
                                  .path(COLUMNS_PATH)
                                  .path(Integer.toString(columnId))
                                  .build();

        final ClientResponse response = httpLowLevel.deleteRaw(uri);
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
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(viewUri)
                                          .path(resourceId)
                                          .path(COLUMNS_PATH)
                                          .path(Integer.toString(payload.getId()))
                                          .build();
                return httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, payload);
            }
        };

        try {
            final ClientResponse response = requester.issueRequest();
            return response.getEntity(Column.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Column.class, requester);
        }
    }

    /**
     * Adds an asset to the Socrata Service.  An Asset is a file stored as a blob on the service.
     *
     * @param file file to upload
     * @return the asset ID and name
     */
    public AssetResponse addAsset(final File file) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<File>(null, file)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                return httpLowLevel.postFileRaw(assetUri, MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_PLAIN_TYPE, payload);
            }
        };

        try {
            final ClientResponse response = requester.issueRequest();
            return mapper.readValue(response.getEntity(InputStream.class), AssetResponse.class);
            //return response.getEntity(AssetResponse.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, AssetResponse.class, requester);
        } catch (JsonMappingException e) {
            throw new SodaError("Illegal response from the service.");
        } catch (JsonParseException e) {
            throw new SodaError("Invalid JSON returned from the service.");
        } catch (IOException e) {
            throw new SodaError("Error communicating with service.");
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
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI uri = UriBuilder.fromUri(assetUri)
                                          .path(resourceId)
                                          .build();

                return httpLowLevel.queryRaw(uri, MediaType.WILDCARD_TYPE);
            }
        };

        try {
            final ClientResponse response = requester.issueRequest();
            return response.getEntity(InputStream.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, InputStream.class, requester);
        }
    }

}
