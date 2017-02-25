package com.socrata.api;

import com.google.common.collect.Lists;
import com.socrata.Resources;
import com.socrata.TestBase;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.UpsertResult;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.utils.GeneralUtils;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.Crime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 */
public class Soda2ProducerTest extends TestBase
{

    private static final File  CRIMES_HEADER_CSV = Resources.file("/testCrimesHeader.csv");
    private static final File  CRIMES_HEADER2_CSV = Resources.file("/testCrimesHeader2.csv");
    private static final File  DELETE_CRIMES_CSV = Resources.file("/testDeletingCrimes.csv");


    /**
     * Tests a number of ways to connect using dataset name
     */
    @Test
    public void testReplace() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel  connection = connect();
        final Soda2Producer producer = new Soda2Producer(connection);
        final SodaImporter  sodaImporter = new SodaImporter(connection);

        DatasetInfo datasetCreated = sodaImporter.createViewFromCsv(name, description, CRIMES_HEADER2_CSV, "ID");
        DatasetInfo datasetPublished = sodaImporter.publish(datasetCreated.getId());

        try {

            List<Crime> results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(3, results.size());


            UpsertResult replaceResult = producer.replaceCsv(datasetPublished.getId(), CRIMES_HEADER_CSV);
            TestCase.assertEquals(2, replaceResult.getRowsCreated());
            TestCase.assertEquals(0, replaceResult.errorCount());
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(2, results.size());

            replaceResult = producer.replace(datasetPublished.getId(), Lists.newArrayList(results.get(0)));
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(1, results.size());


            //Try it with a larger file
            /*
            replaceResult = producer.replaceCsv(datasetPublished.getId(), CRIMES_APPEND_CSV);
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(1000, results.size());
            */

        } finally {
            sodaImporter.deleteDataset(datasetPublished.getId());
        }

    }

    /**
     * Tests a number of ways to connect using dataset name
     */
    @Test
    public void testReplaceStream() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel  connection = connect();
        final Soda2Producer producer = new Soda2Producer(connection);
        final SodaImporter  sodaImporter = new SodaImporter(connection);

        FileInputStream fisCrimes1 = new FileInputStream(CRIMES_HEADER_CSV);
        DatasetInfo datasetCreated = sodaImporter.createViewFromCsv(name, description, CRIMES_HEADER2_CSV, "ID");
        DatasetInfo datasetPublished = sodaImporter.publish(datasetCreated.getId());

        try {

            List<Crime> results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(3, results.size());


            UpsertResult replaceResult = producer.replaceStream(datasetPublished.getId(), HttpLowLevel.CSV_TYPE, fisCrimes1);
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(2, results.size());

            replaceResult = producer.replace(datasetPublished.getId(), Lists.newArrayList(results.get(0)));
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(1, results.size());


        } finally {
            sodaImporter.deleteDataset(datasetPublished.getId());
            GeneralUtils.closeQuietly(fisCrimes1);
        }

    }

    @Test
    public void testSoda2OnWorkingCopy() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {

        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel  connection = connect();
        final Soda2Producer producer = new Soda2Producer(connection);
        final SodaImporter  sodaImporter = new SodaImporter(connection);

        DatasetInfo datasetCreated = sodaImporter.createViewFromCsv(name, description, CRIMES_HEADER2_CSV, "ID");
        DatasetInfo datasetPublished = sodaImporter.publish(datasetCreated.getId());
        DatasetInfo datasetWorkingCopy = sodaImporter.createWorkingCopy(datasetPublished.getId());

        try {

            List<Crime> results = producer.query(datasetWorkingCopy.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(3, results.size());


            UpsertResult replaceResult = producer.replaceCsv(datasetWorkingCopy.getId(), CRIMES_HEADER_CSV);
            results = producer.query(datasetWorkingCopy.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(2, results.size());

            producer.upsertCsv(datasetWorkingCopy.getId(), CRIMES_HEADER2_CSV);
            results = producer.query(datasetWorkingCopy.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(3, results.size());

            /*
            replaceResult = producer.upsertCsv(datasetWorkingCopy.getId(), CRIMES_APPEND_CSV);
            TestCase.assertEquals(3, replaceResult.getRowsUpdated());
            TestCase.assertEquals(997, replaceResult.getRowsCreated());

            results = producer.query(datasetWorkingCopy.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(1000, results.size());
            */

            replaceResult = producer.replace(datasetWorkingCopy.getId(), Lists.newArrayList(results.get(0)));
            results = producer.query(datasetWorkingCopy.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(1, results.size());

            datasetPublished = sodaImporter.publish(datasetWorkingCopy.getId());
        } finally {
            sodaImporter.deleteDataset(datasetPublished.getId());
        }

    }

    /**
     * Tests a number of ways to connect using dataset name
     */
    @Test
    public void testDelete() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel  connection = connect();
        final Soda2Producer producer = new Soda2Producer(connection);
        final SodaImporter  sodaImporter = new SodaImporter(connection);

        DatasetInfo datasetCreated = sodaImporter.createViewFromCsv(name, description, CRIMES_HEADER2_CSV, "ID");
        DatasetInfo datasetPublished = sodaImporter.publish(datasetCreated.getId());

        try {

            List<Crime> results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(3, results.size());


            //Delete two crimes using upsert
            UpsertResult replaceResult = producer.upsertCsv(datasetPublished.getId(), DELETE_CRIMES_CSV);
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(1, results.size());

            producer.delete(datasetPublished.getId(), results.get(0).getId().toString());
            results = producer.query(datasetPublished.getId(), SoqlQuery.SELECT_ALL, Crime.LIST_TYPE);
            TestCase.assertEquals(0, results.size());


        } finally {
            sodaImporter.deleteDataset(datasetPublished.getId());
        }

    }


    public static final String UPSERT_RESULT_NO_ERRORS = "{ \"rows_created\":1, \"rows_updated\":2, \"rows_deleted\":3 }";
    public static final String UPSERT_RESULT_1 = "{ \"rows_created\":1, \"rows_updated\":2, \"rows_deleted\":3, \"errors\":[{\"error\":\"Error1\", \"input_index\":1, \"primary_key\":\"key1\"}] }";
    public static final String UPSERT_RESULT_2 = "{ \"rows_created\":1, \"rows_updated\":2, \"rows_deleted\":3, \"errors\":[{\"error\":\"Error1\", \"input_index\":1, \"primary_key\":\"key1\"},{\"error\":\"Error2\", \"input_index\":2, \"primary_key\":\"key2\"}] }";

    @Test
    public void testParsingUpsertResults() throws IOException
    {
        final Soda2Producer producer = new Soda2Producer(connect());

        UpsertResult noErrors = producer.deserializeUpsertResult(new ByteArrayInputStream(UPSERT_RESULT_NO_ERRORS.getBytes("utf-8")));
        TestCase.assertEquals(0, noErrors.errorCount());
        TestCase.assertEquals(1, noErrors.getRowsCreated());
        TestCase.assertEquals(2, noErrors.getRowsUpdated());
        TestCase.assertEquals(3, noErrors.getRowsDeleted());

        UpsertResult errors1 = producer.deserializeUpsertResult(new ByteArrayInputStream(UPSERT_RESULT_1.getBytes("utf-8")));
        TestCase.assertEquals(1, errors1.errorCount());
        TestCase.assertEquals(1, errors1.getErrors().get(0).getIndex());
        TestCase.assertEquals("Error1", errors1.getErrors().get(0).getError());
        TestCase.assertEquals("key1", errors1.getErrors().get(0).getPrimaryKey());
        TestCase.assertEquals(1, errors1.getRowsCreated());
        TestCase.assertEquals(2, errors1.getRowsUpdated());
        TestCase.assertEquals(3, errors1.getRowsDeleted());

        UpsertResult errors2 = producer.deserializeUpsertResult(new ByteArrayInputStream(UPSERT_RESULT_2.getBytes("utf-8")));
        TestCase.assertEquals(2, errors2.errorCount());
        TestCase.assertEquals(1, errors2.getErrors().get(0).getIndex());
        TestCase.assertEquals("Error1", errors2.getErrors().get(0).getError());
        TestCase.assertEquals("key1", errors2.getErrors().get(0).getPrimaryKey());
        TestCase.assertEquals(2, errors2.getErrors().get(1).getIndex());
        TestCase.assertEquals("Error2", errors2.getErrors().get(1).getError());
        TestCase.assertEquals("key2", errors2.getErrors().get(1).getPrimaryKey());
        TestCase.assertEquals(1, errors2.getRowsCreated());
        TestCase.assertEquals(2, errors2.getRowsUpdated());
        TestCase.assertEquals(3, errors2.getRowsDeleted());
    }


    public static final String SODA_SERVER_UPSERT_RESULT_NO_ERRORS = "[{\"typ\":\"insert\", \"id\":\"key1\", \"ver\":\"1\"}, {\"typ\":\"insert\", \"id\":\"key2\", \"ver\":\"1\"}, {\"typ\":\"insert\", \"id\":\"key3\", \"ver\":\"1\" }]";
    public static final String SODA_SERVER_UPSERT_RESULT_1 =         "[{\"typ\":\"update\", \"id\":\"key1\", \"ver\":\"1\"}, {\"typ\":\"update\", \"id\":\"key2\", \"ver\":\"1\"}, {\"typ\":\"error\", \"id\":\"key3\", \"ver\":\"1\", \"err\":\"error3\" }]";
    public static final String SODA_SERVER_UPSERT_RESULT_2 =         "[{\"typ\":\"delete\", \"id\":\"key1\", \"ver\":\"1\"}, {\"typ\":\"error\",  \"id\":\"key2\", \"ver\":\"1\", \"err\":\"error2\"}, {\"typ\":\"error\", \"id\":\"key3\", \"ver\":\"1\", \"err\":\"error3\" }]";

    @Test
    public void testParsingSodaServerUpsertResults() throws IOException
    {
        final Soda2Producer producer = new Soda2Producer(connect());

        UpsertResult noErrors = producer.deserializeUpsertResult(new ByteArrayInputStream(SODA_SERVER_UPSERT_RESULT_NO_ERRORS.getBytes("utf-8")));
        TestCase.assertEquals(0, noErrors.errorCount());
        TestCase.assertEquals(3, noErrors.getRowsCreated());
        TestCase.assertEquals(0, noErrors.getRowsUpdated());
        TestCase.assertEquals(0, noErrors.getRowsDeleted());

        UpsertResult errors1 = producer.deserializeUpsertResult(new ByteArrayInputStream(SODA_SERVER_UPSERT_RESULT_1.getBytes("utf-8")));
        TestCase.assertEquals(1, errors1.errorCount());
        TestCase.assertEquals("error3", errors1.getErrors().get(0).getError());
        TestCase.assertEquals("key3", errors1.getErrors().get(0).getPrimaryKey());
        TestCase.assertEquals(0, errors1.getRowsCreated());
        TestCase.assertEquals(2, errors1.getRowsUpdated());
        TestCase.assertEquals(0, errors1.getRowsDeleted());

        UpsertResult errors2 = producer.deserializeUpsertResult(new ByteArrayInputStream(SODA_SERVER_UPSERT_RESULT_2.getBytes("utf-8")));
        TestCase.assertEquals(2, errors2.errorCount());
        TestCase.assertEquals("error2", errors2.getErrors().get(0).getError());
        TestCase.assertEquals("key2", errors2.getErrors().get(0).getPrimaryKey());
        TestCase.assertEquals("error3", errors2.getErrors().get(1).getError());
        TestCase.assertEquals("key3", errors2.getErrors().get(1).getPrimaryKey());
        TestCase.assertEquals(0, errors2.getRowsCreated());
        TestCase.assertEquals(0, errors2.getRowsUpdated());
        TestCase.assertEquals(1, errors2.getRowsDeleted());
    }


    public static final String SODA_SERVER_UPSERT_RESULT_NO_ERRORS_OLD = "[[{\"typ\":\"insert\", \"id\":\"key1\", \"ver\":\"1\"}, {\"typ\":\"insert\", \"id\":\"key2\", \"ver\":\"1\"}, {\"typ\":\"insert\", \"id\":\"key3\", \"ver\":\"1\" }]]";
    public static final String SODA_SERVER_UPSERT_RESULT_1_OLD =         "[[{\"typ\":\"update\", \"id\":\"key1\", \"ver\":\"1\"}, {\"typ\":\"update\", \"id\":\"key2\", \"ver\":\"1\"}, {\"typ\":\"error\",  \"id\":\"key3\", \"ver\":\"1\", \"err\":\"error3\" }]]";
    public static final String SODA_SERVER_UPSERT_RESULT_2_OLD =         "[[{\"typ\":\"delete\", \"id\":\"key1\", \"ver\":\"1\"}, {\"typ\":\"error\",  \"id\":\"key2\", \"ver\":\"1\", \"err\":\"error2\"}, {\"typ\":\"error\", \"id\":\"key3\", \"ver\":\"1\", \"err\":\"error3\" }]]";

    @Test
    public void testParsingOldSodaServerUpsertResults() throws IOException
    {
        final Soda2Producer producer = new Soda2Producer(connect());

        UpsertResult noErrors = producer.deserializeUpsertResult(new ByteArrayInputStream(SODA_SERVER_UPSERT_RESULT_NO_ERRORS_OLD.getBytes("utf-8")));
        TestCase.assertEquals(0, noErrors.errorCount());
        TestCase.assertEquals(3, noErrors.getRowsCreated());
        TestCase.assertEquals(0, noErrors.getRowsUpdated());
        TestCase.assertEquals(0, noErrors.getRowsDeleted());

        UpsertResult errors1 = producer.deserializeUpsertResult(new ByteArrayInputStream(SODA_SERVER_UPSERT_RESULT_1_OLD.getBytes("utf-8")));
        TestCase.assertEquals(1, errors1.errorCount());
        TestCase.assertEquals("error3", errors1.getErrors().get(0).getError());
        TestCase.assertEquals("key3", errors1.getErrors().get(0).getPrimaryKey());
        TestCase.assertEquals(0, errors1.getRowsCreated());
        TestCase.assertEquals(2, errors1.getRowsUpdated());
        TestCase.assertEquals(0, errors1.getRowsDeleted());

        UpsertResult errors2 = producer.deserializeUpsertResult(new ByteArrayInputStream(SODA_SERVER_UPSERT_RESULT_2_OLD.getBytes("utf-8")));
        TestCase.assertEquals(2, errors2.errorCount());
        TestCase.assertEquals("error2", errors2.getErrors().get(0).getError());
        TestCase.assertEquals("key2", errors2.getErrors().get(0).getPrimaryKey());
        TestCase.assertEquals("error3", errors2.getErrors().get(1).getError());
        TestCase.assertEquals("key3", errors2.getErrors().get(1).getPrimaryKey());
        TestCase.assertEquals(0, errors2.getRowsCreated());
        TestCase.assertEquals(0, errors2.getRowsUpdated());
        TestCase.assertEquals(1, errors2.getRowsDeleted());
    }
}