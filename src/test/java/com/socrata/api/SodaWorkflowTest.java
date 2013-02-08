package com.socrata.api;

import com.google.common.collect.Lists;
import com.socrata.TestBase;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.importer.Column;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.importer.Metadata;
import com.socrata.model.soql.SoqlQuery;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 */
public class SodaWorkflowTest  extends TestBase
{
    public static final File CRIMES_CSV = new File("src/test/resources/testCrimes.csv");
    public static final File  CRIMES_HEADER_CSV = new File("src/test/resources/testCrimesHeader.csv");


    @Test
    public void testBasicWorkflow() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final HttpLowLevel connection = connect();
        final SodaDdl importer = new SodaDdl(connection);


        final Dataset dataset = createPrivateDataset(createImporter());
        TestCase.assertEquals(DatasetInfo.UNPUBLISHED, dataset.getPublicationStage());

        try {

            //
            //  Publish the dataset
            final DatasetInfo publishedDataset = importer.publish(dataset.getId());
            TestCase.assertEquals(DatasetInfo.PUBLISHED, publishedDataset.getPublicationStage());

            //
            //  Now, modify the metadata and make sure we can save it without having a working copy
            final DatasetInfo datasetInfo = DatasetInfo.copy(dataset);

            if (datasetInfo.getMetadata() == null) {
                datasetInfo.setMetadata(new Metadata());
            }

            datasetInfo.getMetadata().addCustomField("Category", "TestTest", "ValVal");
            importer.updateView(datasetInfo);


            //
            //  Now create a working copy and make sure we can alter columns
            final Dataset workingCopy = (Dataset) importer.createWorkingCopy(dataset.getId());
            final Column newColumn = new Column(null, "new_col", "new_col", "Description", "text", 3, null);
            importer.addColumn(workingCopy.getId(), newColumn);
            importer.updateView(workingCopy);
            final DatasetInfo postPublish = importer.publish(workingCopy.getId());
            final Dataset postPublish2 = importer.loadView(postPublish.getId());

            TestCase.assertEquals(3, postPublish2.getColumns().size());
        } finally {
            importer.deleteView(dataset.getId());
        }
    }

    @Test
    public void testAppendReplace() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        final DatasetInfo datasetInfo = importer.createViewFromCsv("TestCrimes" + UUID.randomUUID().toString(), "Some Chicago Crimes", CRIMES_HEADER_CSV);
        TestCase.assertNotNull(datasetInfo);
        importer.publish(datasetInfo.getId());

        //Work on append
        final DatasetInfo unpublishedView = importer.createWorkingCopy(datasetInfo.getId());
        final DatasetInfo appendResults = importer.append(unpublishedView.getId(), CRIMES_HEADER_CSV, 1, null);


        //Uncomment to test 202s
        //final DatasetInfo appendResults2 = importer.append(unpublishedView.getId(), CRIMES_CSV, 1, null);
        final DatasetInfo appendResults2 = importer.append(unpublishedView.getId(), CRIMES_HEADER_CSV, 1, null);

        final DatasetInfo publishedResults = importer.publish(appendResults.getId());

        List results = consumer.query(publishedResults.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        //TestCase.assertEquals(6, results.size());

        //
        //  Now work on replace
        final DatasetInfo unpublishedView2 = importer.createWorkingCopy(datasetInfo.getId());
        TestCase.assertFalse(unpublishedView2.getId().equals(publishedResults.getId()));

        //Uncomment to test 202s
        //final DatasetInfo appendResults3 = importer.append(unpublishedView2.getId(), CRIMES_CSV, 1, null);
        final DatasetInfo appendResults3 = importer.append(unpublishedView2.getId(), CRIMES_HEADER_CSV, 1, null);
        TestCase.assertNotNull(appendResults3);

        importer.replace(unpublishedView2.getId(), CRIMES_HEADER_CSV, 1, null);
        final DatasetInfo publishedResults2 = importer.publish(unpublishedView2.getId());
        TestCase.assertEquals(publishedResults.getId(), publishedResults2.getId());


        List results2 = consumer.query(publishedResults2.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(2, results2.size());

        //
        //  Now delete
        importer.deleteView(publishedResults2.getId());

    }

    Dataset createPrivateDataset(final SodaImporter importer) throws SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();


        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10)
        ));
        view.setFlags(new ArrayList<String>());

        return importer.createView(view);
    }
}