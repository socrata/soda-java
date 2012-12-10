package com.socrata.api;

import com.google.common.collect.Lists;
import com.socrata.TestBase;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.GeocodingResults;
import com.socrata.model.importer.Column;
import com.socrata.model.importer.Dataset;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 */
public class SodaImporterTest extends TestBase
{

    public static final File  NOMINATIONS_CSV = new File("src/test/resources/testNominations.csv");

    /**
     * Tests a number of ways to connect using dataset name
     */
    @Test
    public void testImport() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Dataset createdView = importer.createViewFromCsv(name, description, NOMINATIONS_CSV);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals(name, createdView.getName());
        TestCase.assertEquals(description, createdView.getDescription());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());

        GeocodingResults results = importer.findPendingGeocodingResults(createdView.getId());
        TestCase.assertEquals(0, results.getTotal());
        TestCase.assertEquals(0, results.getView());

        importer.waitForPendingGeocoding(createdView.getId());
        importer.publish(createdView.getId());

        Dataset loadedView = importer.loadView(createdView.getId());
        TestCase.assertNotNull(loadedView);
        TestCase.assertEquals("published", loadedView.getPublicationStage());

        importer.deleteView(createdView.getId());
    }

    @Test
    public void testSimpleCreate() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);


        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        view.setColumns(new ArrayList<Column>());
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = importer.createView(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());

        importer.publish(createdView.getId());

        createdView.setDescription("New Description");
        final Dataset updatedView = importer.updateView(createdView);
        TestCase.assertEquals("New Description", updatedView.getDescription());
        TestCase.assertEquals("published", updatedView.getPublicationStage());

        Soda2Producer   producer = new Soda2Producer(connection);
        FileInputStream fis = new FileInputStream(NOMINATIONS_CSV);
        producer.upsertStream(updatedView.getId(), HttpLowLevel.CSV_TYPE, fis);
        fis.close();

        Dataset loadedView = importer.loadView(createdView.getId());
        TestCase.assertNotNull(loadedView);
        TestCase.assertEquals("published", loadedView.getPublicationStage());

        importer.deleteView(createdView.getId());
    }


}
