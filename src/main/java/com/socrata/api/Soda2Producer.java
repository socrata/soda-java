package com.socrata.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Meta;
import com.socrata.model.UpsertError;
import com.socrata.model.UpsertResult;
import com.socrata.model.requests.SodaModRequest;
import com.socrata.model.requests.SodaRequest;
import com.socrata.model.requests.SodaTypedRequest;
import com.socrata.utils.GeneralUtils;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * API for sending requests to the SODA server for adding/removing/modifying objects in the datasets.
 *
 * This class does NOT deal with modifying dataset schema.
 */
public class Soda2Producer extends Soda2Consumer
{

    private final JsonFactory factory;

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
        return new Soda2Producer(HttpLowLevel.instantiateBasic(url, userName, password, token, null));
    }

    /**
     * Create a new Soda2Producer object, using the supplied credentials for authentication
     * as well as a 32 character request ID to include in the header when performing publish
     * operations (used for tracking).
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     * @param requestId a 32 character id unique to a single SODA 2 publish operation.  If this is {@code null}, no value will be sent.
     *
     * @return fully configured Soda2Producer
     */
    public static final Soda2Producer newProducerWithRequestId(final String url, String userName, String password, String token, String requestId)
    {
        return new Soda2Producer(HttpLowLevel.instantiateBasic(url, userName, password, token, requestId));
    }


    /**
     * Constructor
     *
     * @param httpLowLevel HttpLowLevel object to use for connecting to the SODA2 service.
     */
    public Soda2Producer(HttpLowLevel httpLowLevel)
    {
        super(httpLowLevel);
        factory = httpLowLevel.getObjectMapper().getFactory();
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
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doTruncate(resourceId); }
        };

        try {
            requester.issueRequest().close();
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
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doDelete(resourceId, payload); }
        };

        try {
            requester.issueRequest().close();
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
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doAdd(resourceId, payload); }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return response.readEntity(Meta.class);
            } finally {
                response.close();
            }
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
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doAdd(resourceId, payload); }
        };

        try {
            Response response = requester.issueRequest();
            try {
                 return response.readEntity(retType);
            } finally {
                response.close();
            }
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
     *   <br>
     *   In order to delete objects using the Upsert function, use the DeleteRecord object to map ":deleted" to
     *   an :id.
     *
     * @param resourceId unique id or resource name of the dataset
     * @param objects  list of objects to upsert
     *
     * @return result of objects added, removed and modified.
     * @throws SodaError  thrown if there is an error.  Investigate the structure for more information.
     * @throws InterruptedException throws is the thread is interrupted.
     */
    public <T> UpsertResult upsert(String resourceId, List<T> objects) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<List<T>>(resourceId, objects)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doAddObjects(resourceId, payload); }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return deserializeUpsertResult(response);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), UpsertResult.class, requester);
        } catch (IOException ioe) {
            throw new SodaError("Error upserting a dataset from this list of objects.  Error message: " + ioe.getLocalizedMessage());
        }
    }


    /**
     * Replaces a dataset with a list of objects.  This is the same as doing a truncate, followed by an upsert, except
     * that it will happen atomically (so you cannot have a failure that puts the dataset in a half state)
     *
     * @param resourceId unique id or resource name of the dataset
     * @param objects list of objects to replace the contents of the dataset with
     * @return Upsert result describing number of objects added/removed as well as errors.
     * @throws SodaError
     * @throws InterruptedException
     */
    public <T> UpsertResult replace(String resourceId, List<T> objects) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<List<T>>(resourceId, objects)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doReplaceObjects(resourceId, payload); }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return deserializeUpsertResult(response);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), UpsertResult.class, requester);
        } catch (IOException ioe) {
            throw new SodaError("Error replacing dataset from this list of objects.  Error message: " + ioe.getLocalizedMessage());
        }
    }

    /**
     * "Upserts" a list of objects.  What this means is that it will:
     * <ol>
     *     <li>Add the objects in the list.</li>
     *     <li>If an object already exists with this value, it will update it.</li>
     *     <li>If the object has a :deleted=true value, the object will be deleted.</li>
     * </ol>
     *   <br>
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
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doAddStream(resourceId, mediaType, payload); }
        };

        try {

            Response response = requester.issueRequest();
            try {
                return deserializeUpsertResult(response);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, mediaType, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<UpsertResult>(InputStream.class), requester);
        } catch (IOException ioe) {
            throw new SodaError("Error upserting a dataset from this stream.  Error message: " + ioe.getLocalizedMessage());
        }
    }


    /**
     * Replaces a dataset with a the objects serialized in an input stream.  This is the same as doing a truncate, followed by an upsert, except
     * that it will happen atomically (so you cannot have a failure that puts the dataset in a half state)
     *
     * @param resourceId unique id or resource name of the dataset
     * @param mediaType what the format of the stream is.  Normally, HttpLowLevel.JSON_TYPE or HttpLowLevel.CSV_TYPE
     * @param stream  JSON stream of objects to replace the existing dataset with.
     *
     * @return Upsert result describing number of objects added/removed as well as errors.
     * @throws SodaError
     * @throws InterruptedException
     */
    public UpsertResult replaceStream(String resourceId, MediaType mediaType, InputStream stream) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaTypedRequest<InputStream>(resourceId, stream, mediaType)
        {
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doReplaceStream(resourceId, mediaType, payload); }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return deserializeUpsertResult(response);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, mediaType, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<UpsertResult>(InputStream.class), requester);
        } catch (IOException ioe) {
            throw new SodaError("Error replacing a dataset from this stream.  Error message: " + ioe.getLocalizedMessage());
        }
    }

    /**
     * "Upserts" a list of objects.  What this means is that it will:
     * <ol>
     *     <li>Add the objects in the list.</li>
     *     <li>If an object already exists with this value, it will update it.</li>
     *     <li>If the object has a :deleted=true value, the object will be deleted.</li>
     * </ol>
     *   <br>
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
                public Response issueRequest() throws LongRunningQueryException, SodaError
                { return doAddStream(resourceId, mediaType, payload); }
            };

            try {
                Response response = requester.issueRequest();
                try {
                    return deserializeUpsertResult(response);
                } finally {
                    response.close();
                }
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
     * Replaces a dataset with the rows defined in the provided CSV.  This is logically the same thing
     * as doing a truncate followed by an upsertCsv, with the advantage of being atomic (so failures can't
     * cause a half-state)
     *
     * @param resourceId unique id or resource name of the dataset
     * @param csvFile File that contains a CSV to replace the dataset with
     * @return Upsert result describing number of objects added/removed as well as errors.
     * @throws SodaError
     * @throws InterruptedException
     */
    public UpsertResult replaceCsv(String resourceId, File csvFile) throws SodaError, InterruptedException
    {
        try {
            InputStream is = new FileInputStream(csvFile);

            SodaRequest requester = new SodaTypedRequest<InputStream>(resourceId, is, HttpLowLevel.CSV_TYPE)
            {
                public Response issueRequest() throws LongRunningQueryException, SodaError
                { return doReplaceStream(resourceId, mediaType, payload); }
            };

            try {
                Response response = requester.issueRequest();
                try {
                    return deserializeUpsertResult(response);
                } finally {
                    response.close();
                }
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
            public Response issueRequest() throws LongRunningQueryException, SodaError
            { return doUpdate(resourceId, id, payload); }
        };

        try {
            Response response = requester.issueRequest();
            try {
                return response.readEntity(Meta.class);
            } finally {
                response.close();
            }
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, HttpLowLevel.JSON_TYPE, e.timeToRetry, getHttpLowLevel().getMaxRetries(), new GenericType<Meta>(Meta.class), requester);
        }

    }

    /**
     * This will return an upsert result, regardless of whether it is
     * using the original response, or the new return from SODA Server
     *
     *
     * @param is
     * @return
     */
    UpsertResult deserializeUpsertResult(InputStream is) throws IOException
    {
        JsonParser parser = factory.createParser(is);


        if (parser.nextToken() == JsonToken.START_ARRAY) {

            int     count = 0;
            long    inserts = 0;
            long    updates = 0;
            long    deletes = 0;
            List<UpsertError> errors = new LinkedList<UpsertError>();


            JsonToken   currToken = parser.nextToken();

            //Eliminate the nested array, in the case this is using an old SODA Server.
            //THis is for backwards compatibility only.
            if (currToken == JsonToken.START_ARRAY) {
                currToken = parser.nextToken();
            }

            while (currToken != JsonToken.END_ARRAY) {

                NewUpsertRow row = parser.readValueAs(NewUpsertRow.class);
                if ("insert".equals(row.typ)) {
                    inserts++;
                } else if ("update".equals(row.typ)) {
                    updates++;
                } else if ("delete".equals(row.typ)) {
                    deletes++;
                } else if ("error".equals(row.typ)) {
                    errors.add(new UpsertError(row.err, count, row.id));
                }

                count++;
                currToken = parser.nextToken();
            }

            return new UpsertResult(inserts, updates, deletes, errors.size() > 0 ? errors : null);
        }

        return parser.readValueAs(UpsertResult.class);
    }

    UpsertResult deserializeUpsertResult(Response response) throws IOException {
        return deserializeUpsertResult(response.readEntity(InputStream.class));
    }

    /**
     * Class that represents a row in the new upsert response stream.
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    static public class NewUpsertRow {
        public final String typ;
        public final String id;
        public final String ver;
        public final String err;

        @JsonCreator
        public NewUpsertRow(final @JsonProperty("typ") String typ,
                            final @JsonProperty("id") String id,
                            final @JsonProperty("ver") String ver,
                            final @JsonProperty("err") String err)
        {
            this.typ = typ;
            this.id = id;
            this.ver = ver;
            this.err = err;
        }
    }

}
