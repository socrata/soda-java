package com.socrata.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.socrata.Resources;
import com.socrata.TestBase;
import com.socrata.builders.BlueprintBuilder;
import com.socrata.builders.ExternalDatasetBuilder;
import com.socrata.builders.NonDataFileDatasetBuilder;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Address;
import com.socrata.model.GeocodingResults;
import com.socrata.model.importer.*;
import com.socrata.model.soql.OrderByClause;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.model.soql.SortOrder;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import test.model.Nomination;
import test.model.NominationsWText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 */
public class SodaImporterTest extends TestBase
{

    public static final File  NOMINATIONS_CSV = Resources.file("/testNominations.csv");
    public static final File  CRIMES_CSV = Resources.file("/testCrimes.csv");
    public static final File  CRIMES_HEADER_CSV = Resources.file("/testCrimesHeader.csv");
    public static final File  BABY_NAMES_LOC = Resources.file("/testBabyNames.csv");
    public static final File  BABY_NAMES_LOC_2 = Resources.file("/testBabyNames2.csv");
    public static final File  BABY_NAMES_LOC_3 = Resources.file("/testBabyNames3.csv");
    public static final File  BRONX_ADMIN_BOUNDARY = Resources.file("/BronxAdminBoundaryLayers.zip");


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
        final DatasetInfo createdView = importer.createViewFromCsv(name, description, NOMINATIONS_CSV);

        try {
            TestCase.assertNotNull(createdView);
            TestCase.assertNotNull(createdView.getId());
            TestCase.assertEquals(name, createdView.getName());
            TestCase.assertEquals(description, createdView.getDescription());
            TestCase.assertEquals("unpublished", createdView.getPublicationStage());

            GeocodingResults results = importer.findPendingGeocodingResults(createdView.getId());
            TestCase.assertEquals(0, results.getView());

            importer.waitForPendingGeocoding(createdView.getId());
            importer.publish(createdView.getId());

            DatasetInfo loadedView = importer.loadDatasetInfo(createdView.getId());
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
            importer.deleteDataset(createdView.getId());
        }
    }

    @Test
    public void testImportWithSetResourceName() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        DatasetInfo createdView = importer.createViewFromCsv(name, description, NOMINATIONS_CSV);

        try {
            //
            //  Make sure data is added
            Soda2Consumer consumer = new Soda2Consumer(connection);
            SoqlQuery   sortQuery = new SoqlQueryBuilder()
                    .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, "name"))
                    .build();
            List<NominationsWText> nominations = consumer.query(createdView.getId(), sortQuery, NominationsWText.LIST_TYPE);
            TestCase.assertEquals(2, nominations.size());
            TestCase.assertEquals("Kitty, Hello", nominations.get(0).getName());
            TestCase.assertEquals("Name, Test", nominations.get(1).getName());



            createdView.setResourceName(name);
            createdView = importer.updateDatasetInfo(createdView);

            consumer = new Soda2Consumer(connection);
            sortQuery = new SoqlQueryBuilder()
                    .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, "name"))
                    .build();
            nominations = consumer.query(createdView.getId(), sortQuery, NominationsWText.LIST_TYPE);
            TestCase.assertEquals(2, nominations.size());
            TestCase.assertEquals("Kitty, Hello", nominations.get(0).getName());
            TestCase.assertEquals("Name, Test", nominations.get(1).getName());

        } finally {
            importer.deleteDataset(createdView.getId());
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

        final DatasetInfo createdView = importer.importScanResults(blueprint, null, NOMINATIONS_CSV, scanResults);



        //final Dataset createdView = importer.createViewFromCsv(name, description, NOMINATIONS_CSV);

        try {
            TestCase.assertNotNull(createdView);
            TestCase.assertNotNull(createdView.getId());
            TestCase.assertEquals(name, createdView.getName());
            TestCase.assertEquals(description, createdView.getDescription());
            TestCase.assertEquals("unpublished", createdView.getPublicationStage());

            GeocodingResults results = importer.findPendingGeocodingResults(createdView.getId());
            TestCase.assertEquals(0, results.getView());

            importer.waitForPendingGeocoding(createdView.getId());
            importer.publish(createdView.getId());

            Dataset loadedView = (Dataset) importer.loadDatasetInfo(createdView.getId());
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
            importer.deleteDataset(createdView.getId());
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

        final Dataset createdView = (Dataset) importer.createDataset(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());

        importer.publish(createdView.getId());

        createdView.setDescription("New Description");
        final Dataset updatedView = (Dataset) importer.updateDatasetInfo(createdView);
        TestCase.assertEquals("New Description", updatedView.getDescription());
        TestCase.assertEquals("published", updatedView.getPublicationStage());

        Soda2Producer   producer = new Soda2Producer(connection);
        FileInputStream fis = new FileInputStream(NOMINATIONS_CSV);
        producer.upsertStream(updatedView.getId(), HttpLowLevel.CSV_TYPE, fis);
        fis.close();

        Dataset loadedView = (Dataset) importer.loadDatasetInfo(createdView.getId());
        TestCase.assertNotNull(loadedView);
        TestCase.assertEquals("published", loadedView.getPublicationStage());

        importer.deleteDataset(loadedView.getId());
    }


    @Test
    public void testSettingPrimaryKey() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        Map<String, String> format = new HashMap<>();
        format.put("noCommas", "true");

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10, format, "Text"),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10, format, "Text")
        ));
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = (Dataset) importer.createDataset(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());


        final Dataset loadView = (Dataset) importer.loadDatasetInfo(createdView.getId());
        TestCase.assertEquals(name, loadView.getName());
        TestCase.assertEquals(2, loadView.getColumns().size());


        //Set license
        loadView.setLicenseId("CC_30_BY_NC");
        loadView.setLicense(new License("Creative Commons Attribution | Noncommercial 3.0 Unported", "images/licenses/cc30bync.png", "http://creativecommons.org/licenses/by-nc/3.0/legalcode"));
        loadView.setAttribution("Socrata Test");
        loadView.setAttributionLink("https://www.socrata.com");

        //Set Metadata
        loadView.setMetadata(new Metadata(ImmutableMap.<String, Map<String, String>>of("geostuff", ImmutableMap.<String, String>of("stuff", "bax")),
                                                                          "col1", null, null, null, null));
        loadView.setRowIdentifierColumnId(loadView.getColumns().get(0).getId());

        loadView.setCategory("Fun");
        loadView.setExternalId("ExternalId_For_Me");

        //Update dataset
        final Dataset updateView2 = (Dataset) importer.updateDatasetInfo(loadView);
        final Dataset loadView2 = (Dataset) importer.loadDatasetInfo(createdView.getId());

        TestCase.assertEquals(loadView.getLicenseId(), loadView2.getLicenseId());
        TestCase.assertEquals(loadView.getLicense().getName(), loadView2.getLicense().getName());
        TestCase.assertEquals(loadView.getLicense().getLogoUrl(), loadView2.getLicense().getLogoUrl());
        TestCase.assertEquals(loadView.getLicense().getTermsLink(), loadView2.getLicense().getTermsLink());

        TestCase.assertEquals(loadView.getCategory(), loadView2.getCategory());
        TestCase.assertEquals(loadView.getExternalId(), loadView2.getExternalId());

        TestCase.assertEquals(loadView.getRowIdentifierColumnId(),       loadView2.getRowIdentifierColumnId());


        TestCase.assertEquals(loadView.getMetadata().getCustom_fields().get("geostuff").get("stuff"), loadView2.getMetadata().getCustom_fields().get("geostuff").get("stuff"));

        TestCase.assertEquals(loadView.getAttribution(), loadView2.getAttribution());
        TestCase.assertEquals(loadView.getAttributionLink(), loadView2.getAttributionLink());

        final Dataset updateView3 = (Dataset) importer.updateDatasetInfo(loadView2);
        final Dataset loadView3 = (Dataset) importer.loadDatasetInfo(createdView.getId());
        TestCase.assertEquals(loadView.getLicenseId(), loadView3.getLicenseId());
        TestCase.assertEquals(loadView.getLicense().getName(), loadView3.getLicense().getName());
        TestCase.assertEquals(loadView.getLicense().getLogoUrl(), loadView3.getLicense().getLogoUrl());
        TestCase.assertEquals(loadView.getLicense().getTermsLink(), loadView3.getLicense().getTermsLink());

        TestCase.assertEquals(loadView.getCategory(), loadView3.getCategory());
        TestCase.assertEquals(loadView.getExternalId(), loadView3.getExternalId());
        TestCase.assertEquals(loadView2.getMetadata().getRowIdentifier(), loadView3.getMetadata().getRowIdentifier());
        TestCase.assertEquals(loadView.getMetadata().getCustom_fields().get("geostuff").get("stuff"), loadView3.getMetadata().getCustom_fields().get("geostuff").get("stuff"));

        TestCase.assertEquals(loadView.getAttribution(), loadView3.getAttribution());
        TestCase.assertEquals(loadView.getAttributionLink(), loadView3.getAttributionLink());

        importer.deleteDataset(createdView.getId());
    }

    @Test
    public void testAttachments() throws LongRunningQueryException, SodaError, InterruptedException, IOException {

        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        Map<String, String> format = new HashMap<>();
        format.put("noCommas", "true");

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10, format, "Text"),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10, format, "Text")
        ));
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = (Dataset) importer.createDataset(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());


        //
        //  Test Adding and getting the Assets
        final AssetResponse response = importer.addAsset(NOMINATIONS_CSV);
        TestCase.assertNotNull(response);
        TestCase.assertNotNull(response.getId());

        final InputStream inputStream = importer.getAsset(response.getId());
        TestCase.assertNotNull(inputStream);

        final String returnedAsset = IOUtils.toString(inputStream);
        final String originalAsset = FileUtils.readFileToString(NOMINATIONS_CSV);
        TestCase.assertEquals(returnedAsset, originalAsset);

        //
        //  Test using assets for attachement
        final Metadata    metadata = new Metadata(null, null, null, null, null, Lists.newArrayList(new Attachment(response.getId(), response.getNameForOutput(), response.getNameForOutput())));
        final Dataset loadView = (Dataset) importer.loadDatasetInfo(createdView.getId());
        loadView.setMetadata(metadata);
        importer.updateDatasetInfo(loadView);

        final Dataset loadView2 = (Dataset) importer.loadDatasetInfo(createdView.getId());
        final Metadata loadedMetadata = loadView2.getMetadata();
        TestCase.assertEquals(1, loadedMetadata.getAttachments().size());
        TestCase.assertEquals(response.getId(), loadedMetadata.getAttachments().get(0).getBlobId());


    }

    @Test
    public void testImportWithPK() throws InterruptedException, SodaError, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String description = name + "-Description";

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);
        final Soda2Producer producer = new Soda2Producer(connection);
        final DatasetInfo createdView = importer.createViewFromCsv(name, description, NOMINATIONS_CSV, "Name");

        try {
            importer.publish(createdView.getId());

            SoqlQuery   sortByNameQuery = new SoqlQueryBuilder(SoqlQuery.SELECT_ALL)
                    .addOrderByPhrase(new OrderByClause(SortOrder.Descending, "name"))
                    .build();

            //Validate
            List retVal =  consumer.query(createdView.getId(), sortByNameQuery, Soda2Consumer.HASH_RETURN_TYPE);
            TestCase.assertEquals(2, retVal.size());
            TestCase.assertEquals("Name, Test", ((Map)retVal.get(0)).get("name"));
            TestCase.assertEquals("TEST (http://www.test.com)", ((Map) retVal.get(0)).get("agency_website"));

            //Test an update that will fail because of a PK constraint
            Nomination duplicatePk = new Nomination("Name, Test", "New Position", "This Agency", "http://foo.bar", new Date(), new Date(), false, false);

            producer.addObject(createdView.getId(), duplicatePk);
            //Verify the existing record was updated, rather than a new one being created
            retVal =  consumer.query(createdView.getId(), sortByNameQuery, Soda2Consumer.HASH_RETURN_TYPE);
            TestCase.assertEquals(2, retVal.size());
            TestCase.assertEquals("Name, Test", ((Map)retVal.get(0)).get("name"));
            TestCase.assertEquals("http://foo.bar", ((Map)retVal.get(0)).get("agency_website"));



        } finally {
            importer.deleteDataset(createdView.getId());
        }


    }

    @Test
    public void testCreatingFileDataset() throws IOException, SodaError, InterruptedException
    {
        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        final String name = "Name" + UUID.randomUUID();
        final String description = "Description" + name;


        final NonDataFileDataset fileDataset = importer.importNonDataFile(name, description, NOMINATIONS_CSV);

        try {
            TestCase.assertNotNull(fileDataset);
            TestCase.assertEquals(name, fileDataset.getName());
            TestCase.assertEquals(description, fileDataset.getDescription());
            TestCase.assertEquals("testNominations.csv", fileDataset.getBlobFilename());
            TestCase.assertEquals(NOMINATIONS_CSV.length(), fileDataset.getBlobFileSize());
            TestCase.assertEquals("published", fileDataset.getPublicationStage());

            importer.makePublic(fileDataset.getId());
            final NonDataFileDataset fileDatasetLoaded = (NonDataFileDataset) importer.loadDatasetInfo(fileDataset.getId());
            TestCase.assertNotNull(fileDatasetLoaded);
            TestCase.assertEquals(name, fileDatasetLoaded.getName());
            TestCase.assertEquals(description, fileDatasetLoaded.getDescription());
            TestCase.assertEquals("testNominations.csv", fileDatasetLoaded.getBlobFilename());
            TestCase.assertEquals(NOMINATIONS_CSV.length(), fileDatasetLoaded.getBlobFileSize());
            TestCase.assertEquals("published", fileDatasetLoaded.getPublicationStage());

            String fileUploadContent = IOUtils.toString(importer.getFileBlob(fileDatasetLoaded));
            String fileContent = FileUtils.readFileToString(NOMINATIONS_CSV);
            TestCase.assertEquals(fileContent, fileUploadContent);



            final NonDataFileDatasetBuilder nonDataFileDatasetBuilder = new NonDataFileDatasetBuilder(fileDatasetLoaded)
                    .addTag("TestFile")
                    .setLicenseId("CC_30_BY_NC")
                    .setLicense(new License("Creative Commons Attribution | Noncommercial 3.0 Unported", "images/licenses/cc30bync.png", "http://creativecommons.org/licenses/by-nc/3.0/legalcode"))
                    .setAttribution("Socrata Test")
                    .setAttributionLink("https://www.socrata.com");

            final NonDataFileDataset fileDatasetLoaded2 = (NonDataFileDataset) importer.updateDatasetInfo(nonDataFileDatasetBuilder.build());
            TestCase.assertEquals("CC_30_BY_NC", fileDatasetLoaded2.getLicenseId());
            TestCase.assertEquals("Socrata Test", fileDatasetLoaded2.getAttribution());
            TestCase.assertEquals("https://www.socrata.com", fileDatasetLoaded2.getAttributionLink());


            final NonDataFileDataset newUploadedFile = importer.replaceNonDataFile(fileDatasetLoaded.getId(), BABY_NAMES_LOC);
            TestCase.assertEquals("CC_30_BY_NC", newUploadedFile.getLicenseId());
            TestCase.assertEquals("Socrata Test", newUploadedFile.getAttribution());
            TestCase.assertEquals("https://www.socrata.com", newUploadedFile.getAttributionLink());
            TestCase.assertFalse(fileDatasetLoaded2.getBlobId().equals(newUploadedFile.getBlobId()));

            String newFileUploadContent = IOUtils.toString(importer.getFileBlob(newUploadedFile));
            String newFileContent = FileUtils.readFileToString(BABY_NAMES_LOC);
            TestCase.assertEquals(newFileContent, newFileUploadContent);


        } finally {
            importer.deleteDataset(fileDataset.getId());
        }

    }


    @Test
    public void testCreatingExternalDataset() throws IOException, SodaError, InterruptedException
    {
        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        final String name = "Name" + UUID.randomUUID();
        final String description = "Description" + name;


        final ExternalDataset    dataset1 = new ExternalDatasetBuilder()
                .setMetadata(new Metadata())
                .addAccessPoint("html", "http://www.google.com")
                .setName(name)
                .setDescription(description)
                .build();

        final ExternalDataset fileDataset = (ExternalDataset) importer.createDataset(dataset1);

        try {
            TestCase.assertNotNull(fileDataset);
            TestCase.assertEquals(name, fileDataset.getName());
            TestCase.assertEquals(description, fileDataset.getDescription());
            TestCase.assertEquals("http://www.google.com", fileDataset.getMetadata().getAccessPoints().get("html"));
            TestCase.assertEquals("unpublished", fileDataset.getPublicationStage());

            importer.publish(fileDataset.getId());
            importer.makePublic(fileDataset.getId());

            final ExternalDataset fileDatasetLoaded = (ExternalDataset) importer.loadDatasetInfo(fileDataset.getId());
            TestCase.assertNotNull(fileDatasetLoaded);
            TestCase.assertEquals(name, fileDatasetLoaded.getName());
            TestCase.assertEquals(description, fileDatasetLoaded.getDescription());
            TestCase.assertEquals("http://www.google.com", fileDatasetLoaded.getMetadata().getAccessPoints().get("html"));
            TestCase.assertEquals("published", fileDatasetLoaded.getPublicationStage());

            final ExternalDatasetBuilder externalDatasetBuilder = new ExternalDatasetBuilder(fileDatasetLoaded)
                    .addTag("TestFile")
                    .setLicenseId("CC_30_BY_NC")
                    .setLicense(new License("Creative Commons Attribution | Noncommercial 3.0 Unported", "images/licenses/cc30bync.png", "http://creativecommons.org/licenses/by-nc/3.0/legalcode"))
                    .setAttribution("Socrata Test")
                    .setAttributionLink("https://www.socrata.com");

            final ExternalDataset fileDatasetLoaded2 = (ExternalDataset) importer.updateDatasetInfo(externalDatasetBuilder.build());
            TestCase.assertEquals("CC_30_BY_NC", fileDatasetLoaded2.getLicenseId());
            TestCase.assertEquals("Socrata Test", fileDatasetLoaded2.getAttribution());
            TestCase.assertEquals("https://www.socrata.com", fileDatasetLoaded2.getAttributionLink());
            TestCase.assertEquals(name, fileDatasetLoaded2.getName());
            TestCase.assertEquals(description, fileDatasetLoaded2.getDescription());
            TestCase.assertEquals("http://www.google.com", fileDatasetLoaded2.getMetadata().getAccessPoints().get("html"));
            TestCase.assertEquals("published", fileDatasetLoaded2.getPublicationStage());

        } finally {
            importer.deleteDataset(fileDataset.getId());
        }

    }

    //@Test
    public void testWithLocations() throws IOException, InterruptedException, SodaError
    {

        final String name = "Testing Loc" ;

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer= new Soda2Consumer(connection);

        ScanResults scanResults = importer.scan(BABY_NAMES_LOC);


        final Blueprint blueprint = new BlueprintBuilder()
                .setName(name)
                .setDescription("Description")
                .setSkip(1)
                .addColumn(new BlueprintColumn("first_name","First Name",   "Text"))
                .addColumn(new BlueprintColumn("county",    "County",       "Text"))
                .addColumn(new BlueprintColumn("sex",       "Sex",          "Number"))
                .addColumn(new BlueprintColumn("count",     "count",        "Number"))
                .addColumn(new BlueprintColumn("rollnumber","RollNumber",   "Number"))
                .addColumn(new BlueprintColumn("street",    "Street",       "Text"))
                .addColumn(new BlueprintColumn("city",      "City",         "Text"))
                .addColumn(new BlueprintColumn("state",     "State",        "Text"))
                .addColumn(new BlueprintColumn("zipcode",   "Zip Code",     "Text"))
                .addColumn(new BlueprintColumn("location",  "location",     "location"))
                .build();


        final String[] translation = new String[] {"col1", "col2","col3",
                "col4","col5","col6","col7","col8","col9", "'(' + col6 + ',' + col7 + ',' + col8 + ',' + col9 + ')'"};
        DatasetInfo dataset = importer.importScanResults(blueprint, translation, BABY_NAMES_LOC, scanResults);

        dataset = importer.publish(dataset.getId());

        //Verify the address got setup correctly
        List results = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(4, results.size());
        for (Object curr : results) {
            Map currRow = (Map)curr;
            Map location = (Map) currRow.get("location");
            Map human_address = (Map) location.get("human_address");

            Address address = new Address((String)human_address.get("address"),
                                          (String)human_address.get("city"),
                                          (String)human_address.get("state"),
                                          (String)human_address.get("zip"));

            TestCase.assertNotNull(location.get("longitude"));
            TestCase.assertNotNull(location.get("latitude"));
            TestCase.assertEquals(currRow.get("street"), address.getStreetAddress());
            TestCase.assertEquals(currRow.get("city"), address.getCity());
            TestCase.assertEquals(currRow.get("state"), address.getState());
            TestCase.assertEquals(currRow.get("zipcode"), address.getZip());
        }


        importer.deleteDataset(dataset.getId());
    }

    //@Test
    public void testWithLocations2() throws IOException, InterruptedException, SodaError
    {

        final String name = "Testing Loc" ;

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer= new Soda2Consumer(connection);
        ScanResults scanResults = importer.scan(BABY_NAMES_LOC_2);


        final Blueprint blueprint = new BlueprintBuilder()
                .setName(name)
                .setDescription("Description")
                .setSkip(1)
                .addColumn(new BlueprintColumn("first_name","First Name",   "Text"))
                .addColumn(new BlueprintColumn("county",    "County",       "Text"))
                .addColumn(new BlueprintColumn("sex",       "Sex",          "Number"))
                .addColumn(new BlueprintColumn("count",     "count",        "Number"))
                .addColumn(new BlueprintColumn("rollnumber","RollNumber",   "Number"))
                .addColumn(new BlueprintColumn("address",    "Address",     "Location"))
                .build();


        DatasetInfo dataset = importer.importScanResults(blueprint, null, BABY_NAMES_LOC_2, scanResults);
        importer.publish(dataset.getId());

        //Verify the address got setup correctly
        List results = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(4, results.size());
        for (Object curr : results) {
            Map currRow = (Map)curr;
            Map location = (Map) currRow.get("address");
            Map human_address = (Map) location.get("human_address");

            Address address = new Address((String)human_address.get("address"),
                                          (String)human_address.get("city"),
                                          (String)human_address.get("state"),
                                          (String)human_address.get("zip"));

            TestCase.assertNotNull(location.get("longitude"));
            TestCase.assertNotNull(location.get("latitude"));
            TestCase.assertNotNull(address.getStreetAddress());
            TestCase.assertNotNull(address.getCity());
            TestCase.assertNotNull(address.getState());
            TestCase.assertNotNull(address.getZip());
        }

        importer.deleteDataset(dataset.getId());
    }

    //@Test
    public void testWithLocations3() throws IOException, InterruptedException, SodaError
    {

        final String name = "Testing Loc" ;

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer= new Soda2Consumer(connection);

        ScanResults scanResults = importer.scan(BABY_NAMES_LOC_3);


        final Blueprint blueprint = new BlueprintBuilder()
                .setName(name)
                .setDescription("Description")
                .setSkip(1)
                .addColumn(new BlueprintColumn("first_name", "First Name", "Text"))
                .addColumn(new BlueprintColumn("county",    "County",       "Text"))
                .addColumn(new BlueprintColumn("sex",       "Sex",          "Number"))
                .addColumn(new BlueprintColumn("count",     "count",        "Number"))
                .addColumn(new BlueprintColumn("rollnumber","RollNumber",   "Number"))
                .addColumn(new BlueprintColumn("lat",       "Lat",          "Number"))
                .addColumn(new BlueprintColumn("long",      "Long",         "Number"))
                .addColumn(new BlueprintColumn("location",  "location",     "location"))
                .build();


        final String[] translation = new String[] {"col1", "col2","col3","col4","col5","col6", "col7","'(' + col6 + ',' + col7 + ')'"};
        DatasetInfo dataset = importer.importScanResults(blueprint, translation, BABY_NAMES_LOC_3, scanResults);

        importer.publish(dataset.getId());

        //Verify the address got setup correctly
        List results = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(4, results.size());
        for (Object curr : results) {
            Map currRow = (Map)curr;
            Map location = (Map) currRow.get("location");
            TestCase.assertNotNull(currRow.get("lat"));
            TestCase.assertNotNull(currRow.get("long"));
            TestCase.assertNotNull(location.get("longitude"));
            TestCase.assertNotNull(location.get("latitude"));
        }

        importer.deleteDataset(dataset.getId());
    }


    @Test
    public void testWithShapefile() throws IOException, InterruptedException, SodaError
    {

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        final GeoDataset dataset = (GeoDataset) importer.createViewFromShapefile(BRONX_ADMIN_BOUNDARY);
        TestCase.assertNotNull(dataset);

        try {
            GeoInfo  geoInfo = dataset.getMetadata().getGeo();
            TestCase.assertNotNull(geoInfo);
            TestCase.assertNotNull(geoInfo.namespace);
            TestCase.assertEquals(7, geoInfo.decodeLayers().length);
            TestCase.assertNotNull(geoInfo.owsUrl);
            TestCase.assertEquals(4, geoInfo.decodeBbox().length);


            final GeoDataset replacedDataset = (GeoDataset) importer.replaceViewFromShapefile(dataset.getId(), BRONX_ADMIN_BOUNDARY);
            TestCase.assertNotNull(replacedDataset);
            TestCase.assertTrue(dataset.getCreatedAt().equals(replacedDataset.getCreatedAt()));
            TestCase.assertFalse(dataset.getViewLastModified().equals(replacedDataset.getViewLastModified()));

        } finally {
            importer.deleteDataset(dataset.getId());
        }
    }

    @Test
    public void testAsyncImports() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        final Dataset view = new Dataset();
        view.setName(name);
        view.setColumns(new ArrayList<Column>());
        view.setFlags(new ArrayList<String>());

        ScanResults scanResults = importer.scan(BABY_NAMES_LOC_3);

        final Blueprint blueprint = new BlueprintBuilder()
                .setName(name)
                .setDescription("Test async imports")
                .setSkip(1)
                .addColumn(new BlueprintColumn("first_name", "First Name", "Text"))
                .addColumn(new BlueprintColumn("county",    "County",       "Text"))
                .build();


        final String[] translation = new String[] {"col1", "col2"};
        DatasetInfo dataset = importer.importScanResults(blueprint, translation, BABY_NAMES_LOC_3, scanResults, true);

        List results = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(results.size(), 4);

        importer.append(dataset.getId(), BABY_NAMES_LOC_3, 1, translation, true);
        results = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(results.size(), 8);

        importer.replace(dataset.getId(), NOMINATIONS_CSV, 1, translation, true);
        results = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(results.size(), 2);
    }

}
