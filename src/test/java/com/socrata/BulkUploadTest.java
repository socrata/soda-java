package com.socrata;

import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Producer;
import com.socrata.api.SodaImporter;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.UpsertResult;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.soql.SoqlQuery;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 */
public class BulkUploadTest extends TestBase
{
    private static final File CRIMES_CSV_HEADER = Resources.file("/testCrimesHeader.csv");
    private static final File CRIMES_CSV_UPSERT = Resources.file("/testCrimesAppend.csv");


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
        TestCase.assertEquals(0, results.errorCount());
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


        final DatasetInfo dataset = importer.createViewFromCsv(name, description, CRIMES_CSV_HEADER);
        TestCase.assertNotNull(dataset);
        TestCase.assertNotNull(dataset.getId());

        UpsertResult result = producer.upsertCsv(dataset.getId(), CRIMES_CSV_UPSERT);
        TestCase.assertNotNull(result);
        TestCase.assertEquals(0, result.errorCount());
        TestCase.assertEquals(4999, result.getRowsCreated());
        TestCase.assertEquals(0, result.getRowsUpdated());
    }

    @Test
    public void testUpsertWithRowIdentifier() throws IOException, SodaError, InterruptedException {
        final Soda2Producer producer = createProducer();
        final SodaImporter importer = createImporter();

        final String name = "RowIdUpsert" + UUID.randomUUID();
        final String description = name + "-Description";

        //Import a CSV and set the rowIdentifier to be ID
        final DatasetInfo dataset = importer.createViewFromCsv(name, description, CRIMES_CSV_HEADER, "ID");
        TestCase.assertNotNull(dataset);
        TestCase.assertNotNull(dataset.getId());
        importer.publish(dataset.getId());

        try {

            //
            //Verify the row we expect is really there.
            final SoqlQuery   lookupTestRow = new SoqlQueryBuilder()
                        .setWhereClause("id='8880962'")
                        .build();
            final List queryResults = producer.query(dataset.getId(), lookupTestRow, Soda2Producer.HASH_RETURN_TYPE);
            TestCase.assertEquals(1, queryResults.size());

            final Map result = (Map) queryResults.get(0);
            TestCase.assertEquals("8880962", result.get("id"));
            TestCase.assertEquals("THEFT", result.get("primary_type"));


            //
            //  Update the dataset by uploading a CSV stream
            final InputStream  csvStream = getClass().getResourceAsStream("/testCrimesHeader2.csv");
            final UpsertResult results = producer.upsertStream(dataset.getId(), HttpLowLevel.CSV_TYPE, csvStream);
            TestCase.assertEquals(1, results.getRowsCreated());
            TestCase.assertEquals(0, results.errorCount());
            TestCase.assertEquals(0, results.getRowsDeleted());
            TestCase.assertEquals(2, results.getRowsUpdated());

            //
            //   Verify an overwrite happened, and not just an append.
            final List queryResults2 = producer.query(dataset.getId(), lookupTestRow, Soda2Producer.HASH_RETURN_TYPE);
            TestCase.assertEquals(1, queryResults.size());

            final Map result2 = (Map) queryResults2.get(0);
            TestCase.assertEquals("8880962", result2.get("id"));
            TestCase.assertEquals("BATTERY", result2.get("primary_type"));

            //
            //  Test adding a stream that has an invalid row in it
            final InputStream  csvStreamInvalid = getClass().getResourceAsStream("/testCrimesWithInvalidCrime.csv");
            final UpsertResult resultsInvalid = producer.upsertStream(dataset.getId(), HttpLowLevel.CSV_TYPE, csvStreamInvalid);
            TestCase.assertEquals(0, resultsInvalid.getRowsCreated());
            TestCase.assertEquals(1, resultsInvalid.errorCount());
            TestCase.assertEquals(0, resultsInvalid.getRowsDeleted());
            TestCase.assertEquals(2, resultsInvalid.getRowsUpdated());

            TestCase.assertEquals(1, resultsInvalid.getErrors().get(0).getIndex());
            TestCase.assertEquals("", resultsInvalid.getErrors().get(0).getPrimaryKey());


        } finally {
            importer.deleteDataset(dataset.getId());
        }

    }


}
