package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.importer.AssetResponse;
import com.socrata.model.importer.Column;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;
import com.sun.jersey.api.client.ClientResponse;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

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


    private final URI assetUri;

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

    }


    /**
     * Creates an empty dataset, based on the view passed in.
     *
     * The new view will be unpublished.
     *
     * @param view view to create the new view on.  The ID should NOT be set.
     * @return the created view, the ID will be set on this.
     * @throws SodaError
     * @throws InterruptedException
     */
    public Dataset createView(final Dataset view) throws SodaError, InterruptedException
    {
        try {
            final ClientResponse response = httpLowLevel.postRaw(viewUri, HttpLowLevel.JSON_TYPE, view);
            return response.getEntity(Dataset.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Dataset.class);
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
    public Dataset loadView(final String id) throws SodaError, InterruptedException
    {

        try {
            final URI uri = UriBuilder.fromUri(viewUri)
                                      .path(id)
                                      .build();
            final ClientResponse response = httpLowLevel.queryRaw(uri, HttpLowLevel.JSON_TYPE);
            return response.getEntity(Dataset.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Dataset.class);
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
    public  Dataset updateView(final DatasetInfo datasetInfo) throws SodaError, InterruptedException
    {
        try {
            URI uri = UriBuilder.fromUri(viewUri)
                                .path(datasetInfo.getId())
                                .build();

            final ClientResponse response = httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, datasetInfo);
            return response.getEntity(Dataset.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Dataset.class);
        }
    }

    /**
     * Deletes a dataset
     *
     * @param id the ID of the dataset to delete
     * @throws SodaError
     * @throws InterruptedException
     */
    public void deleteView(final String id) throws SodaError, InterruptedException
    {
        try {

            URI uri = UriBuilder.fromUri(viewUri)
                                .path(id)
                                .build();

            httpLowLevel.deleteRaw(uri);
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Dataset.class);
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
        final URI uri = UriBuilder.fromUri(viewUri)
                                  .path(datasetId)
                                  .path(COLUMNS_PATH)
                                  .build();

        try {
            final ClientResponse response = httpLowLevel.postRaw(uri, HttpLowLevel.JSON_TYPE, column);
            return response.getEntity(Column.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Column.class);
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
        final URI uri = UriBuilder.fromUri(viewUri)
                                  .path(datasetId)
                                  .path(COLUMNS_PATH)
                                  .path(Integer.toString(column.getId()))
                                  .build();

        try {
            final ClientResponse response = httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, column);
            return response.getEntity(Column.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Column.class);
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
        try {
            final ClientResponse response = httpLowLevel.postFileRaw(assetUri, MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_PLAIN_TYPE, file);
            return mapper.readValue(response.getEntity(InputStream.class), AssetResponse.class);
            //return response.getEntity(AssetResponse.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, AssetResponse.class);
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
        final URI uri = UriBuilder.fromUri(assetUri)
                                  .path(id)
                                  .build();


        try {
            final ClientResponse response = httpLowLevel.queryRaw(uri, MediaType.WILDCARD_TYPE);
            return response.getEntity(InputStream.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, InputStream.class);
        }
    }

}
