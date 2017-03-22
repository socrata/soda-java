package com.socrata.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.socrata.TestBase;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.SearchResults;
import com.socrata.model.importer.*;
import com.socrata.model.search.SearchClause;
import com.socrata.model.soql.SoqlQuery;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * */
public class SodaDdlTest  extends TestBase
{

    @Test
    public void testBasicColumnCrud() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();
        final String formatDetail = "noCommas";

        final HttpLowLevel connection = connect();
        final SodaDdl importer = new SodaDdl(connection);

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));

        Map<String, String> format = new HashMap<>();
        format.put(formatDetail, "true");
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10, format, "Text"),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10, format, "Text")
        ));
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = (Dataset) importer.createDataset(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());

        try {

            final Column  newColumn1 = new Column(0, "newCol1 Name", "new_col_1", "newCol1 Description",
                    "number", 3, 20, format, "Text");

            //Add a column
            final Column retVal1 = importer.addColumn(createdView.getId(), newColumn1);
            TestCase.assertNotNull(retVal1);
            TestCase.assertEquals(newColumn1.getName(), retVal1.getName());
            TestCase.assertEquals(newColumn1.getFieldName(),    retVal1.getFieldName());
            TestCase.assertEquals(newColumn1.getDescription(),  retVal1.getDescription());
            TestCase.assertEquals(newColumn1.getDataTypeName(), retVal1.getDataTypeName());
            TestCase.assertEquals(newColumn1.getFormat().get(formatDetail), retVal1.getFormat().get(formatDetail));

            final Dataset loadedDataset = (Dataset) importer.loadDatasetInfo(createdView.getId());
            TestCase.assertEquals(3, loadedDataset.getColumns().size());
            TestCase.assertEquals(newColumn1.getName(), loadedDataset.getColumns().get(2).getName());
            TestCase.assertEquals(newColumn1.getFieldName(),    loadedDataset.getColumns().get(2).getFieldName());
            TestCase.assertEquals(newColumn1.getDescription(),  loadedDataset.getColumns().get(2).getDescription());
            TestCase.assertEquals(newColumn1.getDataTypeName(), loadedDataset.getColumns().get(2).getDataTypeName());
            TestCase.assertEquals(newColumn1.getFormat().get(formatDetail),
                    loadedDataset.getColumns().get(2).getFormat().get(formatDetail));

            //Update the column
            final Column  newColumn2 = new Column(retVal1.getId(), "newCol2 Name", "new_col_2", "newCol2 Description",
                    "number", 3, 20, format, "number");

            final Column retVal2 = importer.alterColumn(createdView.getId(), newColumn2);
            TestCase.assertNotNull(retVal2);
            TestCase.assertEquals(newColumn2.getName(),         retVal2.getName());
            TestCase.assertEquals(newColumn2.getFieldName(),    retVal2.getFieldName());
            TestCase.assertEquals(newColumn2.getDescription(),  retVal2.getDescription());
            TestCase.assertEquals(newColumn2.getDataTypeName(), retVal2.getDataTypeName());
            TestCase.assertEquals(newColumn2.getFormat().get(formatDetail), retVal2.getFormat().get(formatDetail));

            final Column  newColumn3 = new Column(retVal2.getId(), "newCol2 Name", "new_col_2", "newCol2 Description",
                    "number", 3, 20, format, "number");
            final Column retVal3 = importer.alterColumn(createdView.getId(), newColumn2);
            TestCase.assertNotNull(retVal3);
            TestCase.assertEquals(newColumn3.getName(),         retVal3.getName());
            TestCase.assertEquals(newColumn3.getFieldName(),    retVal3.getFieldName());
            TestCase.assertEquals(newColumn3.getDescription(),  retVal3.getDescription());
            TestCase.assertEquals(newColumn3.getDataTypeName(), retVal3.getDataTypeName());
            TestCase.assertEquals(newColumn3.getFormat().get(formatDetail), retVal3.getFormat().get(formatDetail));

            final Dataset loadedDataset2 = (Dataset) importer.loadDatasetInfo(createdView.getId());
            TestCase.assertEquals(3, loadedDataset2.getColumns().size());
            TestCase.assertEquals(newColumn3.getName(),         loadedDataset2.getColumns().get(2).getName());
            TestCase.assertEquals(newColumn3.getFieldName(),    loadedDataset2.getColumns().get(2).getFieldName());
            TestCase.assertEquals(newColumn3.getDescription(),  loadedDataset2.getColumns().get(2).getDescription());
            TestCase.assertEquals(newColumn3.getDataTypeName(), loadedDataset2.getColumns().get(2).getDataTypeName());
            TestCase.assertEquals(newColumn3.getFormat().get(formatDetail),
                    loadedDataset2.getColumns().get(2).getFormat().get(formatDetail));

            importer.removeColumn(createdView.getId(), newColumn3.getId());
            final Dataset loadedDataset3 = (Dataset) importer.loadDatasetInfo(createdView.getId());
            TestCase.assertEquals(2, loadedDataset3.getColumns().size());

        } finally {
            importer.deleteDataset(createdView.getId());
        }
    }

    @Test
    public void testMetadataCrud() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {

        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        Map<String, String> format = new HashMap<>();
        format.put("noCommas", "true");
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10, format, "Text"),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10, format, "Text")
        ));
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = (Dataset) importer.createDataset(view);
        TestCase.assertNotNull(createdView);
        TestCase.assertNotNull(createdView.getId());
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());

        final Metadata metadata = new Metadata(ImmutableMap.of("Dataset Summary", (Map<String, String>) ImmutableMap.of("Organization", "DDDDDD")), null, null, null, null, null);
        final Dataset loadedView = (Dataset) importer.loadDatasetInfo(createdView.getId());
        loadedView.setMetadata(metadata);
        importer.updateDatasetInfo(loadedView);

        final Dataset loadedView2 = (Dataset) importer.loadDatasetInfo(createdView.getId());
        TestCase.assertEquals(1, loadedView2.getMetadata().getCustom_fields().size());

        importer.publish(createdView.getId());

        final DatasetInfo loadedView3 = new DatasetInfo();
        loadedView3.setId(loadedView2.getId());
        final Metadata metadata2 = new Metadata(ImmutableMap.of("Dataset Summary", (Map<String, String>) ImmutableMap.of("Organization", "FFFFF")), null, null, null, null, null);
        loadedView3.setMetadata(metadata2);
        DatasetInfo loadedDataset = importer.updateDatasetInfo(loadedView3);
        TestCase.assertEquals("FFFFF", loadedDataset.getMetadata().getCustom_fields().get("Dataset Summary").get("Organization"));

        importer.deleteDataset(loadedDataset.getId());
    }


    @Test
    public void testSearch() throws SodaError, InterruptedException, IOException
    {
        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);


        final SearchClause    nameClause = new SearchClause.NameSearch("TestUpdate");
        final SearchClause    tagClause = new SearchClause.TagSearch("test");
        final SearchClause    metadataClause = new SearchClause.MetadataSearch("Tests", "value", "testUpdateMetadata");


        final SearchResults results1 =  importer.searchViews(nameClause);
        TestCase.assertEquals(1, results1.getCount());
        TestCase.assertEquals("TestUpdate", results1.getResults().get(0).getDataset().getName());

        final SearchResults results2 =  importer.searchViews(tagClause);
        TestCase.assertTrue(3 >= results2.getCount());

        final SearchResults results2a =  importer.searchViews(tagClause, nameClause);
        TestCase.assertEquals(1, results2a.getCount());
        TestCase.assertEquals("TestUpdate", results2a.getResults().get(0).getDataset().getName());


        final SearchResults results3 =  importer.searchViews(metadataClause);
        TestCase.assertEquals(1, results3.getCount());
        TestCase.assertEquals("TestUpdate", results3.getResults().get(0).getDataset().getName());

        final SearchResults results4 =  importer.searchViews(nameClause, tagClause, metadataClause);
        TestCase.assertEquals(1, results4.getCount());
        TestCase.assertEquals("TestUpdate", results4.getResults().get(0).getDataset().getName());

        final SearchResults results5 =  importer.searchViews(metadataClause, new SearchClause.ViewTypeSearch(SearchClause.ViewType.dataset));
        TestCase.assertEquals(1, results5.getCount());
        TestCase.assertEquals("TestUpdate", results5.getResults().get(0).getDataset().getName());

        final SearchResults results6 =  importer.searchViews(metadataClause, new SearchClause.ViewTypeSearch(SearchClause.ViewType.view));
        TestCase.assertEquals(0, results6.getCount());

    }

    @Test
    public void testSettingResourceName() throws IOException, SodaError, InterruptedException
    {
        final String name = "Name" + UUID.randomUUID();
        final String resourceName = "resource_" + name.toLowerCase();
        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Producer producer = new Soda2Producer(connection);

        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        Map<String, String> format = new HashMap<>();
        format.put("noCommas", "true");
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10, format, "Text"),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10, format, "Text")
        ));
        view.setResourceName(resourceName);
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = (Dataset)  importer.createDataset(view);

        try {
            importer.publish(createdView.getId());

            producer.addObject(resourceName, ImmutableMap.of("col1", "hello", "col2", "kitty"));
            List queryResults = producer.query(resourceName, SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
            TestCase.assertEquals(1, queryResults.size());

        } finally {
            importer.deleteDataset(createdView.getId());
        }
    }

}
