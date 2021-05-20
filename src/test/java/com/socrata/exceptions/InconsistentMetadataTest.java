package com.socrata.exceptions;

import com.socrata.TestBase;
import com.socrata.api.HttpLowLevel;
import com.socrata.api.SodaImporter;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 */
public class InconsistentMetadataTest  extends TestBase
{

    public static final File  WIDE_CSV = new File("src/test/resources/wideDataset.csv");
    public static final File  TEST_NOMINATIONA_CSV = new File("src/test/resources/testNominations.csv");

    @Ignore
    @Test
    public void testSyncMetadataMessup() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel connection = connect();
        final SodaImporter sodaImporter = new SodaImporter(connection);

        DatasetInfo datasetCreated = sodaImporter.createViewFromCsv(name, description, TEST_NOMINATIONA_CSV, "Name");
        try {
            //Get the ID column
            int columnId = ((Dataset)datasetCreated).getRowIdentifierColumnId();
            sodaImporter.removeColumn(datasetCreated.getId(), columnId);


            try {
                sodaImporter.append(datasetCreated.getId(), TEST_NOMINATIONA_CSV, 1, null);
                TestCase.fail("Expected failure after updating a non-existing column as a rowidentifier");
            } catch (MetadataUpdateError mue) {
                 //Success error expected
            }

        } finally {
            sodaImporter.deleteDataset(datasetCreated.getId());
        }

    }


    @Test
    public void testAsyncMetadataMessup() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel connection = connect();
        final SodaImporter sodaImporter = new SodaImporter(connection);

        DatasetInfo datasetCreated = sodaImporter.createViewFromCsv(name, description, WIDE_CSV, "col0");
        try {
            //Get the ID column
            int columnId = ((Dataset)datasetCreated).getRowIdentifierColumnId();
            sodaImporter.removeColumn(datasetCreated.getId(), columnId);

            /*
            try {
                sodaImporter.append(datasetCreated.getId(), WIDE_CSV, 1, null);
                TestCase.fail("Expected failure after updating a non-existing column as a rowidentifier");
            } catch (MetadataUpdateError mue) {
                //Success error expected
            }
            */
        } finally {
            sodaImporter.deleteDataset(datasetCreated.getId());
        }

    }
}
