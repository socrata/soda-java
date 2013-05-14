package com.socrata.api;

import com.socrata.builders.BlueprintBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.importer.*;
import com.socrata.model.requests.SodaRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.InBoundHeaders;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;

/**
 * This class contains all the apis for using the full file import/update apis.
 *
 * The update and append APIs in this class require the dataset is in a working copy.  Since, creating
 * and publishing working copies can be expensive, when operating on large datsets or doing frequent updates
 * you should use the Soda2Producer class.  Soda2Producer does not require creating working copies, however,
 * when doing very large changes  or replacing a dataset working copies can be useful.
 *
 * Look at http://dev.socrata.com/publishers/workflow for information about the workflow process.
 *
 */
public class SodaImporter extends SodaDdl
{

    public static final String SCAN_BASE_PATH = "imports2";


    private final URI           importUri;


    /**
     * Create a new SodaImporter object, using the supplied credentials for authentication.
     *
     * @param url the base URL for the SODA2 domain to access.
     * @param userName user name to log in as
     * @param password password to log in with
     * @param token the App Token to use for authorization and usage tracking.  If this is {@code null}, no value will be sent.
     *
     * @return fully configured SodaImporter
     */
    public static final SodaImporter newImporter(final String url, String userName, String password, String token)
    {
        return new SodaImporter(HttpLowLevel.instantiateBasic(url, userName, password, token));
    }

    /**
     * Constructor.
     *
     * @param httpLowLevel the HttpLowLevel this uses to contact the server
     */
    public SodaImporter(HttpLowLevel httpLowLevel)
    {
        super(httpLowLevel);

        importUri = httpLowLevel.uriBuilder()
                              .path(API_BASE_PATH)
                              .path(SCAN_BASE_PATH)
                              .build();


    }

    /**
     * Creates a dataset from a CSV, using all the default column types.  This will also
     * assume the CSV has a single header row at the top.
     *
     * @param name name of the dataset to create
     * @param description description of the new dataset
     * @param file the file to upload
     * @param rowIdentifierColumnName row identifie
     * @return return the view that was just created.
     *
     * @throws InterruptedException
     * @throws SodaError
     * @throws IOException
     */
    public DatasetInfo createViewFromCsv(final String name, final String description, final File file, @Nullable final String rowIdentifierColumnName) throws InterruptedException, SodaError, IOException
    {
        return importScanResults(name, description, file, scan(file), rowIdentifierColumnName);
    }


    /**
     * Creates a dataset from a CSV, using all the default column types.  This will also
     * assume the CSV has a single header row at the top.
     *
     * @param name name of the dataset to create
     * @param description description of the new dataset
     * @param file the file to upload
     * @return return the view that was just created.
     *
     * @throws InterruptedException
     * @throws SodaError
     * @throws IOException
     */
    public DatasetInfo createViewFromCsv(final String name, final String description, final File file) throws InterruptedException, SodaError, IOException
    {
        return importScanResults(name, description, file, scan(file));
    }


    /**
     * Scans a file, then sends it up to the Socrata service to be analyzed and have things
     * like column types guessed.
     *
     * @param file File to upload
     * @return the results of the scan.
     *
     * @throws SodaError
     * @throws InterruptedException
     */
    public ScanResults scan(final File file) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<File>(null, file)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI scanUri = UriBuilder.fromUri(importUri)
                                              .queryParam("method", "scan")
                                              .build();

                return httpLowLevel.postFileRaw(scanUri, HttpLowLevel.CSV_TYPE, payload);
            }
        };

        try {
            final ClientResponse response = requester.issueRequest();
            return response.getEntity(ScanResults.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), ScanResults.class, requester);
        }
    }




    /**
     * Imports the results of scanning a file.  This will build  a default blueprint from it, assuming the first rows are
     * column names.
     *
     * @param name name of the dataset to create
     * @param description description of the datset
     * @param file file that was scanned
     * @param scanResults results of the scan
     * @return The default View object for the dataset that was just created.
     */
    public DatasetInfo importScanResults(final String name, final String description, final File file, final ScanResults scanResults) throws SodaError, InterruptedException, IOException
    {
       return importScanResults(name, description, file, scanResults, null);
    }


    /**
     * Imports the results of scanning a file.  This will build  a default blueprint from it, assuming the first rows are
     * column names.
     *
     * @param name name of the dataset to create
     * @param description description of the datset
     * @param file file that was scanned
     * @param scanResults results of the scan
     * @return The default View object for the dataset that was just created.
     */
    public DatasetInfo importScanResults(final String name, final String description, final File file, final ScanResults scanResults, @Nullable final String rowIdentifierColumnName) throws SodaError, InterruptedException, IOException
    {
        final Blueprint blueprint = new BlueprintBuilder(scanResults)
                                        .setSkip(1)
                                        .setName(name)
                                        .setDescription(description)
                                        .build();

        DatasetInfo createdDatasetInfo = importScanResults(blueprint, null, file, scanResults);

        if (rowIdentifierColumnName != null) {

            final Dataset createdDataset = (Dataset) loadDatasetInfo(createdDatasetInfo.getId());
            try {
                createdDataset.setupRowIdentifierColumnByName(rowIdentifierColumnName);
                createdDatasetInfo = updateDatasetInfo(createdDataset);
            } catch (IllegalArgumentException e) {
                deleteDataset(createdDataset.getId());
                throw e;
            }
        }
        return createdDatasetInfo;
    }


    /**
     * Imports the results of scanning a file.  This method does not assume anything about the CSV, but instead has
     * the caller provide the blueprint and the translation for any schema defintion or data transforms.
     *
     * @param blueprint
     * @param translation
     * @param file file that was scanned
     * @param scanResults results of the scan
     * @return The default View object for the dataset that was just created.
     */
    public DatasetInfo importScanResults(final Blueprint blueprint, final String[] translation, final File file, final ScanResults scanResults) throws SodaError, InterruptedException, IOException
    {
        final String blueprintString = mapper.writeValueAsString(blueprint);

        final String blueprintBody = "blueprint="+URLEncoder.encode(blueprintString, "UTF-8");
        return sendScanResults(blueprintBody, scanResults.getFileId(), translation, file);
    }


    /**
     * This appends the contents of a file to a dataset on Socrata.  This operation requires the dataset is
     * in a working copy, so unless you are doing large updates or your dataset is small, using the UPSERT functionality
     * in Soda2Producer may give you better results.
     *
     * If you are doing frequent updates, the apis in Soda2Producer may give better results (since they don't require working copies)
     *
     * In the case of errors, if the error is an MetadataUpdateError, then the data has all been committed, but there was a problem with
     * the meta-data.  In the case of any other errors, the dataset is in an unknown state.  The only way to get it back into a clean
     * state is to remove the working copy, and start again.  The Soda2Producer API has better error semantics where all rows will be
     * either committed or rolledback.
     *
     *
     * @param datasetId  id of the dataset to append to
     * @param file file with the data in it
     * @param skip number of rows in the data to skip (normally for skipping headers)
     * @param translation an optional translation array for translating from values in the file and values in the dataset.
     * @return The info of the dataset after the append operation.
     * @throws com.socrata.exceptions.MetadataUpdateError thrown if the data was updated, but the process failed because
     * of a metadata inconsistency.  In this case, the data has already been committed.
     */
    public DatasetInfo append(String datasetId, File file, int skip, final String[] translation) throws SodaError, InterruptedException, IOException
    {

        final ScanResults results = scan(file);
        return updateFromScanResults(datasetId, "append", skip, results.getFileId(), translation, file);
    }

    /**
     * This replaces the contents of a file to a dataset on Socrata.  This operation requires the dataset is
     * in a working copy, which is an expensive operation.  If your dataset is large, you may want to figure out
     * how to figure out which rows to update, rather than doing a full replace for updates.
     *
     * If you are doing frequent updates, the apis in Soda2Producer may give better results (since they don't require working copies)
     *
     * In the case of errors, if the error is an MetadataUpdateError, then the data has all been committed, but there was a problem with
     * the meta-data.  In the case of any other errors, the dataset is in an unknown state.  The only way to get it back into a clean
     * state is to remove the working copy, and start again.  The Soda2Producer API has better error semantics where all rows will be
     * either committed or rolledback.
     *
     * @param datasetId  id of the dataset to append to
     * @param file file with the data in it
     * @param skip number of rows in the data to skip (normally for skipping headers)
     * @param translation an optional translation array for translating from values in the file and values in the dataset.
     * @return The info of the dataset after the append operation.
     * @throws com.socrata.exceptions.MetadataUpdateError thrown if the data was updated, but the process failed because
     * of a metadata inconsistency.  In this case, the data has already been committed.
     */
    public DatasetInfo replace(String datasetId, File file, int skip, final String[] translation) throws SodaError, InterruptedException, IOException
    {
        final ScanResults results = scan(file);
        return updateFromScanResults(datasetId, "replace", skip, results.getFileId(), translation, file);
    }

    protected DatasetInfo updateFromScanResults(final String datasetId, final String method, final int skip, final String fileId, final String[] translation, final File file) throws SodaError, InterruptedException, IOException
    {


        final StringBuilder updateBody = new StringBuilder();
        updateBody.append("viewUid=").append(datasetId)
                  .append("&method=").append(method)
                  .append("&skip=").append(skip);


        return sendScanResults(updateBody.toString(), fileId, translation, file);

    }

    protected DatasetInfo sendScanResults(final String basePostBody, final String fileId, final String[] translation, final File file) throws SodaError, InterruptedException, IOException
    {

        final StringBuilder postbodyBuilder = new StringBuilder(basePostBody);

        final String translationString =  (translation != null) ? "[" + StringUtils.join(translation, ",") + "]" : "";

        postbodyBuilder.append("&fileId=").append(fileId)
                       .append("&translation=").append(URLEncoder.encode(translationString, "UTF-8"))
                       .append("&name=").append(URLEncoder.encode(file.getName(), "UTF-8"));

        SodaRequest requester = new SodaRequest<String>(null, postbodyBuilder.toString())
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                return httpLowLevel.postRaw(importUri, MediaType.APPLICATION_FORM_URLENCODED_TYPE, payload);
            }
        };


        try {

            final ClientResponse response = requester.issueRequest();
            return response.getEntity(DatasetInfo.class);
        } catch (LongRunningQueryException e) {

            if (e.location != null) {
                return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, Integer.MAX_VALUE, DatasetInfo.class, requester);
            } else {

                final URI ticketUri = UriBuilder.fromUri(importUri)
                                                .queryParam("ticket", e.ticket)
                                                .build();
                return getHttpLowLevel().getAsyncResults(ticketUri, e.timeToRetry, Integer.MAX_VALUE, Dataset.class, requester);

            }
        }

    }

    /**
     * Imports a file that is NOT going to be used to create a datset, but is instead available for
     * downloading directly.
     *
     * @param name name of the file
     * @param description description of the file
     * @param file the file to upload
     * @return The NonDataFileDataset object that was saved to Socrata
     */
    public NonDataFileDataset importNonDataFile(final String name, final String description, final File file) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<File>(null, file)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI scanUri = UriBuilder.fromUri(importUri)
                                              .queryParam("method", "blob")
                                              .queryParam("fileUploaderfile", file.getName())
                                              .build();

                try {
                    final InputStream   is = new FileInputStream(file);
                    try {
                        ClientResponse clientResponse = httpLowLevel.postFileRaw(scanUri, MediaType.APPLICATION_OCTET_STREAM_TYPE, MediaType.TEXT_PLAIN_TYPE, file);

                        //Funny issue with service, currently only returns MediaType.TEXT_PLAIN_TYPE, but the
                        //response needs to be processed as JSON.  So, wrap the return in a ClientResponse that acts
                        //as if the content type is JSON. There is a bug on the core server side to fix this.
                        InBoundHeaders  headers = new InBoundHeaders();
                        headers.putSingle("Content-Type", MediaType.APPLICATION_JSON);
                        return new ClientResponse(clientResponse.getStatus(), headers, clientResponse.getEntityInputStream(), clientResponse.getClient().getMessageBodyWorkers());

                    } finally {
                        is.close();
                    }
                } catch (IOException ioe) {
                    throw new SodaError("Unable to load file: " + file.getAbsolutePath());
                }
            }
        };

        NonDataFileDataset nonDataFileDataset;
        try {
            final ClientResponse response = requester.issueRequest();
            nonDataFileDataset = response.getEntity(NonDataFileDataset.class);
        } catch (LongRunningQueryException e) {
            nonDataFileDataset = getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), NonDataFileDataset.class, requester);
        }

        nonDataFileDataset.setDescription(description);
        nonDataFileDataset.setName(name);
        return (NonDataFileDataset) updateDatasetInfo(nonDataFileDataset);
    }


    /**
     * Replaces the file blob for a Imports a NonDataFileDataset.  For changing other properties,
     * use SodaDdl.updateDatasetInfo
     *
     * @param id name of the file
     * @param file the file to upload
     * @return The NonDataFileDataset object that was saved to Socrata
     */
    public NonDataFileDataset replaceNonDataFile(final String id, final File file) throws SodaError, InterruptedException
    {

        SodaRequest requester = new SodaRequest<File>(null, file)
        {
            public ClientResponse issueRequest() throws LongRunningQueryException, SodaError
            {
                final URI scanUri = UriBuilder.fromUri(viewUri)
                                              .path(id + ".txt")
                                              .queryParam("method", "replaceBlob")
                                              .queryParam("fileUploaderfile", file.getName())
                                              .build();

                try {
                    final InputStream   is = new FileInputStream(file);
                    try {
                        ClientResponse clientResponse = httpLowLevel.postFileRaw(scanUri, MediaType.APPLICATION_OCTET_STREAM_TYPE, MediaType.TEXT_PLAIN_TYPE, file);

                        //Funny issue with service, currently only returns MediaType.TEXT_PLAIN_TYPE, but the
                        //response needs to be processed as JSON.  So, wrap the return in a ClientResponse that acts
                        //as if the content type is JSON. There is a bug on the core server side to fix this.
                        InBoundHeaders  headers = new InBoundHeaders();
                        headers.putSingle("Content-Type", MediaType.APPLICATION_JSON);
                        return new ClientResponse(clientResponse.getStatus(), headers, clientResponse.getEntityInputStream(), clientResponse.getClient().getMessageBodyWorkers());

                    } finally {
                        is.close();
                    }
                } catch (IOException ioe) {
                    throw new SodaError("Unable to load file: " + file.getAbsolutePath());
                }
            }
        };

        NonDataFileDataset nonDataFileDataset;
        try {
            final ClientResponse response = requester.issueRequest();
            nonDataFileDataset = response.getEntity(NonDataFileDataset.class);
        } catch (LongRunningQueryException e) {
            nonDataFileDataset = getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, getHttpLowLevel().getMaxRetries(), NonDataFileDataset.class, requester);
        }

        return (NonDataFileDataset) loadDatasetInfo(id);
    }


    /**
     * Creates a straight translation with no transforms for a  given bluprint.
     *
     * @param blueprint blueprint to build the translation from
     * @return the array of mappings to map each field to itself.  This will create a translation that will do nothing.
     */
    public String[] generateTranslation(final Blueprint blueprint) {
        final String[]    retVal = new String[blueprint.getColumns().size()];

        int i =0;
        for (BlueprintColumn column : blueprint.getColumns()) {
            //retVal[i++] = column.getName();
            retVal[i] = "col" + i;
            i++;
        }

        return retVal;
    }



}
