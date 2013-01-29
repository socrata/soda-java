package com.socrata.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.socrata.TestBase;
import com.socrata.builders.BlueprintBuilder;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.GeocodingResults;
import com.socrata.model.importer.*;
import com.socrata.model.soql.OrderByClause;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.model.soql.SortOrder;
import com.sun.jersey.api.client.GenericType;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.Nomination;
import test.model.NominationWithJoda;
import test.model.NominationsWText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 */
public class SodaImporterTest extends TestBase
{

    public static final File  NOMINATIONS_CSV = new File("src/test/resources/testNominations.csv");
    public static final File  CRIMES_CSV = new File("src/test/resources/testCrimes.csv");
    public static final File  CRIMES_HEADER_CSV = new File("src/test/resources/testCrimesHeader.csv");

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

        try {
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

            final Soda2Consumer consumer = new Soda2Consumer(connection);
            final SoqlQuery   sortQuery = new SoqlQueryBuilder()
                    .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, "name"))
                    .build();
            List<NominationsWText> nominations = consumer.query(createdView.getId(), sortQuery, NominationsWText.LIST_TYPE);
            TestCase.assertEquals(2, nominations.size());
            TestCase.assertEquals("Kitty, Hello", nominations.get(0).getName());
            TestCase.assertEquals("Name, Test", nominations.get(1).getName());
        } finally {
            importer.deleteView(createdView.getId());
        }
    }

    @Test
    public void testWithTypes() throws IOException, InterruptedException, SodaError, LongRunningQueryException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final ScanResults  scanResults = importer.scan(NOMINATIONS_CSV);
        final Blueprint    blueprint = new BlueprintBuilder()
                .setName(name)
                .setDescription(description)
                .setSkip(1)
                .addColumn(new BlueprintColumn("Name", "Name Description", "Text"))
                .addColumn(new BlueprintColumn("Position", "Position Description", "Text"))
                .addColumn(new BlueprintColumn("Agency Name", "Agency Name Description", "Text"))
                .addColumn(new BlueprintColumn("Agency Website", "Agency Website Description", "Text"))
                .addColumn(new BlueprintColumn("Nomination Date", "Nomination Date Description", "calendar_date"))
                .addColumn(new BlueprintColumn("Confirmation Vote", "Confirmation Vote Description", "calendar_date"))
                .addColumn(new BlueprintColumn("Confirmed", "Confirmed Description", "checkbox"))
                .addColumn(new BlueprintColumn("Holdover", "Holdover Description", "checkbox"))
                .build();

        final Dataset createdView = importer.importScanResults(blueprint, null, NOMINATIONS_CSV, scanResults);



        //final Dataset createdView = importer.createViewFromCsv(name, description, NOMINATIONS_CSV);

        try {
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

            final Soda2Consumer consumer = new Soda2Consumer(connection);
            final SoqlQuery   sortQuery = new SoqlQueryBuilder()
                    .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, "name"))
                    .build();
            List<Nomination> nominations = consumer.query(createdView.getId(), sortQuery, Nomination.LIST_TYPE);
            TestCase.assertEquals(2, nominations.size());
            TestCase.assertEquals("Kitty, Hello", nominations.get(0).getName());
            TestCase.assertEquals("Name, Test", nominations.get(1).getName());
        } finally {
            importer.deleteView(createdView.getId());
        }
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


    @Test
    public void testSettingPrimaryKey() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);


        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10)
        ));
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = importer.createView(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());


        final Dataset loadView = importer.loadView(createdView.getId());
        TestCase.assertEquals(name, loadView.getName());
        TestCase.assertEquals(2, loadView.getColumns().size());


        //Set license
        loadView.setLicenseId("CC_30_BY_NC");
        loadView.setLicense(new License("Creative Commons Attribution-Noncommercial 3.0 Unported", "images/licenses/cc30bync.png", "http://creativecommons.org/licenses/by-nc/3.0/legalcode"));
        loadView.setAttribution("Socrata Test");
        loadView.setAttributionLink("https://www.socrata.com");

        //Set Metadata
        loadView.setMetadata(new Metadata(ImmutableMap.<String, Map<String, String>>of("geostuff", ImmutableMap.<String, String>of("stuff", "bax")),
                                                                          "col1", null, null));
        loadView.setRowIdentifierColumnId(loadView.getColumns().get(0).getId());

        loadView.setCategory("Fun");
        loadView.setExternalId("ExternalId_For_Me");

        //Update dataset
        final Dataset updateView2 = importer.updateView(loadView);
        final Dataset loadView2 = importer.loadView(createdView.getId());

        TestCase.assertEquals(loadView.getLicenseId(), loadView2.getLicenseId());
        TestCase.assertEquals(loadView.getLicense().getName(), loadView2.getLicense().getName());
        TestCase.assertEquals(loadView.getLicense().getLogoUrl(), loadView2.getLicense().getLogoUrl());
        TestCase.assertEquals(loadView.getLicense().getTermsLink(), loadView2.getLicense().getTermsLink());

        TestCase.assertEquals(loadView.getCategory(), loadView2.getCategory());
        TestCase.assertEquals(loadView.getExternalId(), loadView2.getExternalId());

        TestCase.assertEquals(loadView.getRowIdentifierColumnId(),       loadView2.getRowIdentifierColumnId());


        TestCase.assertEquals(loadView.getMetadata().getCustom_metadata().get("geostuff").get("stuff"), loadView2.getMetadata().getCustom_metadata().get("geostuff").get("stuff"));

        TestCase.assertEquals(loadView.getAttribution(), loadView2.getAttribution());
        TestCase.assertEquals(loadView.getAttributionLink(), loadView2.getAttributionLink());

        final Dataset updateView3 = importer.updateView(loadView2);
        final Dataset loadView3 = importer.loadView(createdView.getId());
        TestCase.assertEquals(loadView.getLicenseId(), loadView3.getLicenseId());
        TestCase.assertEquals(loadView.getLicense().getName(), loadView3.getLicense().getName());
        TestCase.assertEquals(loadView.getLicense().getLogoUrl(), loadView3.getLicense().getLogoUrl());
        TestCase.assertEquals(loadView.getLicense().getTermsLink(), loadView3.getLicense().getTermsLink());

        TestCase.assertEquals(loadView.getCategory(), loadView3.getCategory());
        TestCase.assertEquals(loadView.getExternalId(), loadView3.getExternalId());
        TestCase.assertEquals(loadView2.getMetadata().getRowIdentifier(), loadView3.getMetadata().getRowIdentifier());
        TestCase.assertEquals(loadView.getMetadata().getCustom_metadata().get("geostuff").get("stuff"), loadView3.getMetadata().getCustom_metadata().get("geostuff").get("stuff"));

        TestCase.assertEquals(loadView.getAttribution(), loadView3.getAttribution());
        TestCase.assertEquals(loadView.getAttributionLink(), loadView3.getAttributionLink());

        importer.deleteView(createdView.getId());
    }


}
