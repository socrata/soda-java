package com.socrata;

import com.socrata.TestBase;
import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Producer;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.UpsertResult;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 */
public class BulkUploadTest extends TestBase
{
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


}
