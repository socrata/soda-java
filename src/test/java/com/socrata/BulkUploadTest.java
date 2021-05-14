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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
     * Will issue a simple query, and do spot validation.
     * TODO: EN-45878
     */
    @Ignore
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

    // TODO: EN-45878
    @Ignore
    @Test
    public void testGiantUpsert() throws IOException, SodaError, InterruptedException
    {
        final Soda2Producer producer = createProducer();
        final SodaImporter importer = createImporter();

        final String name = "LongUpsertName" + UUID.randomUUID();
        final String description = name + "-Description";

        final DatasetInfo dataset = importer.createViewFromCsv(name, description, CRIMES_CSV_HEADER);
        TestCase.assertNotNull(dataset);
        TestCase.assertNotNull(dataset.getId());

        InputStream stream = new InputStream() {
            boolean headerRead = false;
            File file = CRIMES_CSV_UPSERT;
            BufferedReader reader;
            ByteArrayInputStream buffer;
            int count = 0;

            public int read() throws IOException {
                byte[] b = new byte[1];
                int count = read(b);
                return (count <= 0) ? -1 : (b[0] & 0xff);
            }

            @Override
            public int read(byte[] b) throws IOException {
                return read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if(!refill()) {
                    return -1;
                }
                return buffer.read(b, off, len);
            }

            @Override
            public void close() throws IOException {
                if(reader != null) reader.close();
            }

            private boolean refill() throws IOException {
                if(buffer == null || buffer.available() == 0) {
                    if(reader == null) {
                        if(count++ > 1000) {
                            return false;
                        }
                        System.out.println("Producing copy " + count);
                        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                        if(headerRead) reader.readLine();
                        else headerRead = true;
                    }
                    String line = reader.readLine();
                    if(line == null) { reader.close(); reader = null; return refill(); }
                    buffer = new ByteArrayInputStream((line + "\n").getBytes(StandardCharsets.UTF_8));
                }
                return true;
            }
        };
        UpsertResult result = producer.upsertStream(dataset.getId(), HttpLowLevel.CSV_TYPE, stream);
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
            // Verify the row we expect is really there.
            final SoqlQuery lookupTestRow = new SoqlQueryBuilder()
                        .setWhereClause("id='8880962'")
                        .build();
            Thread.sleep(5000); // EN-45880
            final List queryResults = producer.query(dataset.getId(), lookupTestRow, Soda2Producer.HASH_RETURN_TYPE);
            TestCase.assertEquals(1, queryResults.size());

            final Map result = (Map) queryResults.get(0);
            TestCase.assertEquals("8880962", result.get("id"));
            TestCase.assertEquals("THEFT", result.get("primary_type"));

            // Update the dataset by uploading a CSV stream
            final InputStream  csvStream = getClass().getResourceAsStream("/testCrimesHeader2.csv");
            final UpsertResult results = producer.upsertStream(dataset.getId(), HttpLowLevel.CSV_TYPE, csvStream);
            TestCase.assertEquals(1, results.getRowsCreated());
            TestCase.assertEquals(0, results.errorCount());
            TestCase.assertEquals(0, results.getRowsDeleted());
            TestCase.assertEquals(2, results.getRowsUpdated());

            // Verify an overwrite happened, and not just an append.
            Thread.sleep(5000); // EN-45880
            final List queryResults2 = producer.query(dataset.getId(), lookupTestRow, Soda2Producer.HASH_RETURN_TYPE);
            TestCase.assertEquals(1, queryResults.size());

            final Map result2 = (Map) queryResults2.get(0);
            TestCase.assertEquals("8880962", result2.get("id"));
            TestCase.assertEquals("BATTERY", result2.get("primary_type"));

            /*
            // TODO: EN-45878
            // Test adding a stream that has an invalid row in it
            final InputStream  csvStreamInvalid = getClass().getResourceAsStream("/testCrimesWithInvalidCrime.csv");

            final UpsertResult resultsInvalid = producer.upsertStream(dataset.getId(), HttpLowLevel.CSV_TYPE, csvStreamInvalid);
            TestCase.assertEquals(0, resultsInvalid.getRowsCreated());
            TestCase.assertEquals(1, resultsInvalid.errorCount());
            TestCase.assertEquals(0, resultsInvalid.getRowsDeleted());
            TestCase.assertEquals(2, resultsInvalid.getRowsUpdated());

            TestCase.assertEquals(1, resultsInvalid.getErrors().get(0).getIndex());
            TestCase.assertEquals("", resultsInvalid.getErrors().get(0).getPrimaryKey());
            */
        } finally {
            importer.deleteDataset(dataset.getId());
        }
    }
}
