package com.socrata.api;

import com.google.common.collect.Collections2;
import com.socrata.builders.BlueprintBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.GeocodingResults;
import com.socrata.model.importer.*;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;

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
     * @return return the view that was just created.
     *
     * @throws InterruptedException
     * @throws SodaError
     * @throws IOException
     */
    public Dataset createViewFromCsv(final String name, final String description, final File file) throws InterruptedException, SodaError, IOException
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
        try {
            final URI scanUri = UriBuilder.fromUri(importUri)
                                          .queryParam("method", "scan")
                                          .build();

            final ClientResponse response = httpLowLevel.postFileRaw(scanUri, HttpLowLevel.CSV_TYPE, file);
            return response.getEntity(ScanResults.class);
        } catch (LongRunningQueryException e) {
            return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, HttpLowLevel.DEFAULT_MAX_RETRIES, ScanResults.class);
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
    public Dataset importScanResults(final String name, final String description, final File file, final ScanResults scanResults) throws SodaError, InterruptedException, IOException
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
    public Dataset importScanResults(final String name, final String description, final File file, final ScanResults scanResults, @Nullable final String rowIdentifierColumnName) throws SodaError, InterruptedException, IOException
    {
        final Blueprint blueprint = new BlueprintBuilder(scanResults)
                                        .setSkip(1)
                                        .setName(name)
                                        .setDescription(description)
                                        .build();

        Dataset createdDataset = importScanResults(blueprint, null, file, scanResults);

        if (rowIdentifierColumnName != null) {
            Column  rowIdentifierColumn = null;
            for (Column column : createdDataset.getColumns()) {
                if (rowIdentifierColumnName.equals(column.getName())) {
                    rowIdentifierColumn = column;
                    break;
                }
            }

            if (rowIdentifierColumn == null) {
                final String columnNames = StringUtils.join(Collections2.transform(createdDataset.getColumns(), Column.TO_NAME), ",");
                throw new IllegalArgumentException("No column named " + rowIdentifierColumnName + " exists for this dataset.  " +
                                                           "Current column names are: " + columnNames);
            }

            createdDataset.setupRowIdentifierColumn(rowIdentifierColumn);
            createdDataset = updateView(createdDataset);
        }
        return createdDataset;
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
    public Dataset importScanResults(final Blueprint blueprint, final String[] translation, final File file, final ScanResults scanResults) throws SodaError, InterruptedException, IOException
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
     * @param datasetId  id of the dataset to append to
     * @param file file with the data in it
     * @param skip number of rows in the data to skip (normally for skipping headers)
     * @param translation an optional translation array for translating from values in the file and values in the dataset.
     * @return The info of the dataset after the append operation.
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
     * @param datasetId  id of the dataset to append to
     * @param file file with the data in it
     * @param skip number of rows in the data to skip (normally for skipping headers)
     * @param translation an optional translation array for translating from values in the file and values in the dataset.
     * @return The info of the dataset after the append operation.
     */
    public DatasetInfo replace(String datasetId, File file, int skip, final String[] translation) throws SodaError, InterruptedException, IOException
    {
        final ScanResults results = scan(file);
        return updateFromScanResults(datasetId, "replace", skip, results.getFileId(), translation, file);
    }

    protected Dataset updateFromScanResults(final String datasetId, final String method, final int skip, final String fileId, final String[] translation, final File file) throws SodaError, InterruptedException, IOException
    {


        final StringBuilder updateBody = new StringBuilder();
        updateBody.append("viewUid=").append(datasetId)
                  .append("&method=").append(method)
                  .append("&skip=").append(skip);


        return sendScanResults(updateBody.toString(), fileId, translation, file);

    }

    protected Dataset sendScanResults(final String basePostBody, final String fileId, final String[] translation, final File file) throws SodaError, InterruptedException, IOException
    {
        try {
            final StringBuilder postbodyBuilder = new StringBuilder(basePostBody);

            final String translationString =  (translation != null) ? "[" + StringUtils.join(translation, ",") + "]" : "";

            postbodyBuilder.append("&fileId=").append(fileId)
                           .append("&translation=").append(translationString)
                           .append("&name=").append(URLEncoder.encode(file.getName(), "UTF-8"));

            final String postData = postbodyBuilder.toString();

            final ClientResponse response = httpLowLevel.postRaw(importUri, MediaType.APPLICATION_FORM_URLENCODED_TYPE, postData);
            return response.getEntity(Dataset.class);
        } catch (LongRunningQueryException e) {

            if (e.location != null) {
                return getHttpLowLevel().getAsyncResults(e.location, e.timeToRetry, Integer.MAX_VALUE, Dataset.class);
            } else {

                final URI ticketUri = UriBuilder.fromUri(importUri)
                                                .queryParam("ticket", e.ticket)
                                                .build();
                return getHttpLowLevel().getAsyncResults(ticketUri, e.timeToRetry, Integer.MAX_VALUE, Dataset.class);

            }
        }

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
