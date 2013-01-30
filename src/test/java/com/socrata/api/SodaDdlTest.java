package com.socrata.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.socrata.TestBase;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.importer.Column;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.License;
import com.socrata.model.importer.Metadata;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * */
public class SodaDdlTest  extends TestBase
{

    @Test
    public void testBasicColumnCrud() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaDdl importer = new SodaDdl(connection);

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

        try {

            final Column  newColumn1 = new Column(0, "newCol1 Name", "new_col_1", "newCol1 Description", "number", 3, 20);

            //Add a column
            final Column retVal1 = importer.addColumn(createdView.getId(), newColumn1);
            TestCase.assertNotNull(retVal1);
            TestCase.assertEquals(newColumn1.getName(), retVal1.getName());
            TestCase.assertEquals(newColumn1.getFieldName(),    retVal1.getFieldName());
            TestCase.assertEquals(newColumn1.getDescription(),  retVal1.getDescription());
            TestCase.assertEquals(newColumn1.getDataTypeName(), retVal1.getDataTypeName());

            final Dataset loadedDataset = importer.loadView(createdView.getId());
            TestCase.assertEquals(3, loadedDataset.getColumns().size());
            TestCase.assertEquals(newColumn1.getName(), loadedDataset.getColumns().get(2).getName());
            TestCase.assertEquals(newColumn1.getFieldName(),    loadedDataset.getColumns().get(2).getFieldName());
            TestCase.assertEquals(newColumn1.getDescription(),  loadedDataset.getColumns().get(2).getDescription());
            TestCase.assertEquals(newColumn1.getDataTypeName(), loadedDataset.getColumns().get(2).getDataTypeName());

            //Update the column
            final Column  newColumn2 = new Column(retVal1.getId(), "newCol2 Name", "new_col_2", "newCol2 Description", "number", 3, 20);

            final Column retVal2 = importer.alterColumn(createdView.getId(), newColumn2);
            TestCase.assertNotNull(retVal2);
            TestCase.assertEquals(newColumn2.getName(),         retVal2.getName());
            TestCase.assertEquals(newColumn2.getFieldName(),    retVal2.getFieldName());
            TestCase.assertEquals(newColumn2.getDescription(),  retVal2.getDescription());
            TestCase.assertEquals(newColumn2.getDataTypeName(), retVal2.getDataTypeName());

            final Column  newColumn3 = new Column(retVal2.getId(), "newCol2 Name", "new_col_2", "newCol2 Description", "number", 3, 20);
            final Column retVal3 = importer.alterColumn(createdView.getId(), newColumn2);
            TestCase.assertNotNull(retVal3);
            TestCase.assertEquals(newColumn3.getName(),         retVal3.getName());
            TestCase.assertEquals(newColumn3.getFieldName(),    retVal3.getFieldName());
            TestCase.assertEquals(newColumn3.getDescription(),  retVal3.getDescription());
            TestCase.assertEquals(newColumn3.getDataTypeName(), retVal3.getDataTypeName());

            final Dataset loadedDataset2 = importer.loadView(createdView.getId());
            TestCase.assertEquals(3, loadedDataset2.getColumns().size());
            TestCase.assertEquals(newColumn3.getName(),         loadedDataset2.getColumns().get(2).getName());
            TestCase.assertEquals(newColumn3.getFieldName(),    loadedDataset2.getColumns().get(2).getFieldName());
            TestCase.assertEquals(newColumn3.getDescription(),  loadedDataset2.getColumns().get(2).getDescription());
            TestCase.assertEquals(newColumn3.getDataTypeName(), loadedDataset2.getColumns().get(2).getDataTypeName());

            importer.removeColumn(createdView.getId(), newColumn3.getId());
            final Dataset loadedDataset3 = importer.loadView(createdView.getId());
            TestCase.assertEquals(2, loadedDataset3.getColumns().size());

        } finally {
            importer.deleteView(createdView.getId());
        }
    }

    @Test
    public void testMetadataCrud() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {

        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaDdl importer = new SodaDdl(connection);

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

        final Metadata metadata = new Metadata(ImmutableMap.of("Dataset Summary", (Map<String, String>) ImmutableMap.of("Organization", "DDDDDD")), null, null, null, null);
        final Dataset loadedView = importer.loadView(createdView.getId());
        loadedView.setMetadata(metadata);
        importer.updateView(loadedView);

        final Dataset loadedView2 = importer.loadView(createdView.getId());
        TestCase.assertEquals(1, loadedView2.getMetadata().getCustom_fields().size());

    }

}
