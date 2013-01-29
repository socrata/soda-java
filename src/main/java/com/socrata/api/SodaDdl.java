package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.importer.Column;
import com.socrata.model.importer.Dataset;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Provides APIs for altering the shape of a dataset, whether that be adding columns,
 * removing columns, etc.
 */
public class SodaDdl
{
    public static final String IMPORTER_BASE_PATH = "api";
    public static final String VIEWS_BASE_PATH = "views";
    public static final String COLUMNS_PATH = "columns";


    protected final HttpLowLevel  httpLowLevel;
    protected final URI viewUri;

    /**
     * Constructor.
     *
     * @param httpLowLevel the HttpLowLevel this uses to contact the server
     */
    public SodaDdl(HttpLowLevel httpLowLevel)
    {
        this.httpLowLevel = httpLowLevel;

        viewUri = httpLowLevel.uriBuilder()
                              .path(IMPORTER_BASE_PATH)
                              .path(VIEWS_BASE_PATH)
                              .build();

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
     * Creates an empty dataset, based on the view passed in.
     *
     * The new view will be unpublished.
     *
     * @param view view to create the new view on.  The ID should NOT be set.
     * @return the created view, the ID will be set on this.
     * @throws SodaError
     * @throws InterruptedException
     */
    public Dataset createView(Dataset view) throws SodaError, InterruptedException
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
     * Updates a view.
     *
     * @param view the view to update to.  The ID MUST be set.
     * @return the view after the update.
     *
     * @throws SodaError
     * @throws InterruptedException
     */
    public  Dataset updateView(Dataset view) throws SodaError, InterruptedException
    {
        try {
            URI uri = UriBuilder.fromUri(viewUri)
                                .path(view.getId())
                                .build();

            final ClientResponse response = httpLowLevel.putRaw(uri, HttpLowLevel.JSON_TYPE, view);
            return response.getEntity(Dataset.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, Dataset.class);
        }
    }

    /**
     * Deletes a view
     *
     * @param id the ID of the view to delete
     * @throws SodaError
     * @throws InterruptedException
     */
    public void deleteView(String id) throws SodaError, InterruptedException
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
    public Column addColumn(String datasetId, Column column) throws SodaError, InterruptedException
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
    public void removeColumn(String datasetId, int columnId) throws LongRunningQueryException, SodaError
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
    public Column alterColumn(String datasetId, Column column) throws SodaError, InterruptedException
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


}
