package com.socrata;

import com.socrata.TestBase;
import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Producer;
import com.socrata.api.SodaImporter;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.UpsertResult;
import com.socrata.model.importer.Dataset;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 */
public class BulkUploadTest extends TestBase
{
    public static final File CRIMES_CSV_HEADER = new File("src/test/resources/testCrimesHeader.csv");
    public static final File CRIMES_CSV_UPSERT = new File("src/test/resources/testCrimesAppend.csv");


    /**
     * Tests uploading some records via CSV
     *
     * Will issue a simple query, and do spot validation.
     */
    @Test
    public void testCsvUpload() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final Soda2Producer producer = createProducer();
        final InputStream  csvStream = getClass().getResourceAsStream("/testNominations.csv");

        final UpsertResult results = producer.upsertStream(UPDATE_DATA_SET, HttpLowLevel.CSV_TYPE, csvStream);
        TestCase.assertEquals(2, results.getRowsCreated());
        TestCase.assertEquals(0, results.getErrors());
        TestCase.assertEquals(0, results.getRowsDeleted());
        TestCase.assertEquals(0, results.getRowsUpdated());

    }

    @Test
    public void testLongUpsert() throws IOException, SodaError, InterruptedException
    {

        final Soda2Producer producer = createProducer();
        final SodaImporter importer = createImporter();

        final String name = "LongUpsertName" + UUID.randomUUID();
        final String description = name + "-Description";


        final Dataset dataset = importer.createViewFromCsv(name, description, CRIMES_CSV_HEADER);
        TestCase.assertNotNull(dataset);
        TestCase.assertNotNull(dataset.getId());

        UpsertResult result = producer.upsertCsv(dataset.getId(), CRIMES_CSV_UPSERT);
        TestCase.assertNotNull(result);
        TestCase.assertEquals(4999, result.getRowsCreated());
        TestCase.assertEquals(0, result.getRowsUpdated());



    }


}
