package com.socrata;

import com.google.common.collect.Lists;
import com.socrata.api.*;
import com.socrata.builders.BlueprintBuilder;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Location;
import com.socrata.model.Meta;
import com.socrata.model.importer.*;
import com.socrata.model.soql.SoqlQuery;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.LocationTestClass;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This runs through a number of tests with Locations
 */
public class LocationTests extends TestBase
{

    private static final File LOCATION_TEST_CSV = Resources.file("/locationImports.csv");


    /**
     * Tests pulling out Floating Timestamps from SODA2, and surfacing them as Dates.
     */
    @Test
    public void createLocationTable() throws IOException, InterruptedException, SodaError
    {

        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaDdl importer = new SodaDdl(connection);
        final Soda2Producer producer = new Soda2Producer(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        Map<String, String> format = new HashMap<>();
        format.put("noCommas", "true");

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Location", "Blue"));
        view.setColumns(Lists.newArrayList(
                new Column(0, "uniqueId", "uniqueid", "A unique ID for this table", "Number", 0, 10, format, "Number"),
                new Column(0, "name", "name", "The name of this", "Text", 0, 10, format, "Text"),
                new Column(0, "location", "location", "A location", "Location", 0, 10, format, "Location")
        ));
        view.setFlags(new ArrayList<String>());

        final DatasetInfo createdView = importer.createDataset(view);


        LocationTestClass   obj1 = new LocationTestClass(2, new Location(83.121212, 84.121212, null), "Name 1");
        Meta m = producer.addObject(createdView.getId(), obj1);
        TestCase.assertNotNull(m);

        List<LocationTestClass> retVal = consumer.query(createdView.getId(), SoqlQuery.SELECT_ALL, LocationTestClass.LIST_TYPE);
        TestCase.assertEquals(1, retVal.size());

        LocationTestClass   result = retVal.get(0);
        TestCase.assertEquals(83.121212, result.getLocation().getLongitude());
        TestCase.assertEquals(84.121212, result.getLocation().getLatitude());
        TestCase.assertNull(result.getLocation().getAddress());
        TestCase.assertEquals(2, result.getUniqueid());
        TestCase.assertEquals("Name 1", result.getName());

        importer.deleteDataset(createdView.getId());
    }

    /**
     * Tests pulling out Floating Timestamps from SODA2, and surfacing them as Dates.
     */
    @Test
    public void createLocationTableWithAppend() throws IOException, InterruptedException, SodaError
    {

        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaDdl importer = new SodaDdl(connection);
        final Soda2Producer producer = new Soda2Producer(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        Map<String, String> format = new HashMap<>();
        format.put("noCommas", "true");

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Location", "Blue"));
        view.setColumns(Lists.newArrayList(
                new Column(0, "uniqueId", "uniqueid", "A unique ID for this table", "Number", 0, 10, format, "Number"),
                new Column(0, "name", "name", "The name of this", "Text", 0, 10, format, "Text")
        ));
        view.setFlags(new ArrayList<String>());

        final DatasetInfo createdView = importer.createDataset(view);

        importer.addColumn(createdView.getId(),
                new Column(0, "location", "location", "A location", "Location", 0, 10, format, "Location"));



        LocationTestClass   obj1 = new LocationTestClass(2, new Location(83.121212, 84.121212, null), "Name 1");
        Meta m = producer.addObject(createdView.getId(), obj1);
        TestCase.assertNotNull(m);

        List<LocationTestClass> retVal = consumer.query(createdView.getId(), SoqlQuery.SELECT_ALL, LocationTestClass.LIST_TYPE);
        TestCase.assertEquals(1, retVal.size());

        LocationTestClass   result = retVal.get(0);
        TestCase.assertEquals(83.121212, result.getLocation().getLongitude());
        TestCase.assertEquals(84.121212, result.getLocation().getLatitude());
        TestCase.assertNull(result.getLocation().getAddress());
        TestCase.assertEquals(2, result.getUniqueid());
        TestCase.assertEquals("Name 1", result.getName());

        importer.deleteDataset(createdView.getId());
    }

    /**
     * Tests pulling out Floating Timestamps from SODA2, and surfacing them as Dates.
     */
    @Test
    public void importLocationTable() throws IOException, InterruptedException, SodaError
    {

        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Producer producer = new Soda2Producer(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        ScanResults scanResults = importer.scan(LOCATION_TEST_CSV);


        final Blueprint blueprint = new BlueprintBuilder()
                .setName(name)
                .setDescription("Description")
                .setSkip(1)
                .addColumn(new BlueprintColumn("uniqueId", "UniqueId", "Number"))
                .addColumn(new BlueprintColumn("name", "name", "Text"))
                .addColumn(new BlueprintColumn("location", "location", "location"))
                .build();


        final String[] translation = new String[] {"col1", "col2", "'(' + col4 + ',' + col3 + ')'"};
        DatasetInfo dataset = importer.importScanResults(blueprint, translation, LOCATION_TEST_CSV, scanResults);



        List<LocationTestClass> retVal = consumer.query(dataset.getId(), SoqlQuery.SELECT_ALL, LocationTestClass.LIST_TYPE);
        TestCase.assertEquals(1, retVal.size());

        LocationTestClass   result = retVal.get(0);
        TestCase.assertEquals(83.121212, result.getLocation().getLongitude());
        TestCase.assertEquals(84.121212, result.getLocation().getLatitude());
        TestCase.assertNull(result.getLocation().getAddress());
        TestCase.assertEquals(2, result.getUniqueid());
        TestCase.assertEquals("Name 1", result.getName());

        importer.deleteDataset(dataset.getId());
    }
}
