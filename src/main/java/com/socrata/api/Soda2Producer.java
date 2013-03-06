package com.socrata.api;

import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.UpsertResult;
import com.socrata.model.Meta;
import com.socrata.model.requests.SodaModRequest;
import com.socrata.model.requests.SodaRequest;
import com.socrata.model.requests.SodaTypedRequest;
import com.socrata.utils.GeneralUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * API for sending requests to the SODA server for adding/removing/modifying objects in the datasets.
 *
 * This class does NOT deal with modifying dataset schema.
 */
public class Soda2Producer extends Soda2Consumer
{

    /**
     * Create a new Soda2Producer object, using the supplied credentials for authentication.
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     *
     * @return fully configured Soda2Producer
     */
    public static final Soda2Producer newProducer(final String url, String userName, String password, String token)
    {
        return new Soda2Producer(HttpLowLevel.instantiateBasic(url, userName, password, token));
    }


    /**
     * Constructor
     *
     * @param httpLowLevel HttpLowLevel object to use for connecting to the SODA2 service.
     */
    public Soda2Producer(HttpLowLevel httpLowLevel)
    {
        super(httpLowLevel);
    }


    /**
     * Truncates a dataset by deleting all rows in the dataset.
     *
     * @param resourceId id of the dataset to truncate
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public void truncate(String resourceId) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<String>(resourceId, null)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doTruncate(resourceId); }
        };

        try {
            requester.issueRequest();
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<String>(String.class), requester);
        }
    }

    /**
     * Deletes a row from a dataset.
     *
     * @param resourceId resourceId id of the dataset to delete a record from
     * @param id id of the record to delete.  This should be the unique id of the record, which could be
     *           either the id the system sets, or the Row Identifier.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public void delete(String resourceId, String id) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<String>(resourceId, id)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doDelete(resourceId, payload); }
        };

        try {
            requester.issueRequest();
        } catch (LongRunningQueryException e) {
            getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<String>(String.class), requester);
        }

    }

    /**
     * Add an object using SODA2, the object will be added to the dataset with the specified resource ID.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param object object to add
     *
     * @return the metadata of the row added, including the system identifier
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> Meta addObject(String resourceId, T object) throws SodaError, InterruptedException
    {
        SodaRequest requester = new SodaRequest<T>(resourceId, object)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doAdd(resourceId, payload); }
        };

        try {
            ClientResponse response = requester.issueRequest();
            return response.getEntity(Meta.class);
        } catch (LongRunningQueryException e) {
            return (Meta) getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), Meta.class, requester);
        }
    }

    /**
     * Add an object using SODA2, the object will be added to the dataset with the specified resource ID.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param object object to add
     * @param retType the type of object to return
     *
     * @return the metadata of the row added, including the system identifier
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> T addObject(String resourceId, T object, Class<T> retType) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<T>(resourceId, object)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doAdd(resourceId, payload); }
        };

        try {
            ClientResponse response = requester.issueRequest();
            return response.getEntity(retType);
        } catch (LongRunningQueryException e) {
            return (T) getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), retType, requester);
        }
    }


    /**
     * "Upserts" a list of objects.  What this means is that it will:
     * <ol>
     *     <li>Add the objects in the list.</li>
     *     <li>If an object already exists with this value, it will update it.</li>
     *     <li>If the object has a :deleted=true value, the object will be deleted.</li>
     * </ol>
     *   <br/>
     *   In order to delete objects using the Upsert function, use the DeleteRecord object to map ":deleted" to
     *   an :id.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param objects  list of objects to upsert;
     *
     * @return result of objects added, removed and modified.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public UpsertResult upsert(String resourceId, List objects) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<List>(resourceId, objects)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doAddObjects(resourceId, payload); }
        };

        try {
            ClientResponse response = requester.issueRequest();
            return response.getEntity(UpsertResult.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), UpsertResult.class, requester);
        }
    }

    /**
     * "Upserts" a list of objects.  What this means is that it will:
     * <ol>
     *     <li>Add the objects in the list.</li>
     *     <li>If an object already exists with this value, it will update it.</li>
     *     <li>If the object has a :deleted=true value, the object will be deleted.</li>
     * </ol>
     *   <br/>
     *   In order to delete objects using the Upsert function, use the DeleteRecord object to map ":deleted" to
     *   an :id.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param mediaType what the format of the stream is.  Normally, HttpLowLevel.JSON_TYPE or HttpLowLevel.CSV_TYPE
     * @param stream  JSON stream of objects to update
     *
     * @return result of objects added, removed and modified.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public UpsertResult upsertStream(String resourceId, MediaType mediaType, InputStream stream) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaTypedRequest<InputStream>(resourceId, stream, mediaType)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doAddStream(resourceId, mediaType, payload); }
        };

        try {

            ClientResponse response = requester.issueRequest();
            return response.getEntity(UpsertResult.class);

        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, mediaType, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<UpsertResult>(InputStream.class), requester);
        }
    }


    /**
     * "Upserts" a list of objects.  What this means is that it will:
     * <ol>
     *     <li>Add the objects in the list.</li>
     *     <li>If an object already exists with this value, it will update it.</li>
     *     <li>If the object has a :deleted=true value, the object will be deleted.</li>
     * </ol>
     *   <br/>
     *   In order to delete objects using the Upsert function, use the DeleteRecord object to map ":deleted" to
     *   an :id.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param csvFile File that contains a CSV to upload
     *
     * @return result of objects added, removed and modified.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public UpsertResult upsertCsv(String resourceId, File csvFile) throws SodaError, InterruptedException
    {
        try {
            InputStream is = new FileInputStream(csvFile);

            SodaRequest requester = new SodaTypedRequest<InputStream>(resourceId, is, HttpLowLevel.CSV_TYPE)
            {
                public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
                { return doAddStream(resourceId, mediaType, payload); }
            };

            try {
                ClientResponse response = requester.issueRequest();
                return response.getEntity(UpsertResult.class);
            } catch (LongRunningQueryException e) {
                return getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.CSV_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<UpsertResult>(InputStream.class), requester);
            } finally {
                GeneralUtils.closeQuietly(is);
            }
        } catch (IOException ioe) {
            throw new SodaError("Cannot load CSV from the file " + GeneralUtils.bestFilePath(csvFile) + ".  Error message: " + ioe.getLocalizedMessage());
        }
    }



    /**
     * Updates an object in a dataset.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param id  Id based on a dataset specific unique column, or the system ID created for each row.
     * @param object object to update the result with
     *
     * @return
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> Meta  update(String resourceId, Object id, T object) throws SodaError, InterruptedException
    {
        SodaRequest requester = new SodaModRequest<T>(resourceId, object, id)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            { return doUpdate(resourceId, id, payload); }
        };

        try {

            ClientResponse response = requester.issueRequest();
            return response.getEntity(Meta.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<Meta>(Meta.class), requester);
        }

    }

}
