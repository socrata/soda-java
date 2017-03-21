package com.socrata.api;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.socrata.Resources;
import com.socrata.TestBase;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Comment;
import com.socrata.model.importer.*;
import com.socrata.model.soql.SoqlQuery;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 */
public class SodaWorkflowTest  extends TestBase
{
    private static final File CRIMES_CSV = Resources.file("/testCrimes.csv");
    private static final File  CRIMES_HEADER_CSV = Resources.file("/testCrimes.csv");


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
            importer.updateDatasetInfo(datasetInfo);


            //
            //  Now create a working copy and make sure we can alter columns
            final Dataset workingCopy = (Dataset) importer.createWorkingCopy(dataset.getId());
            HashMap<String, String> format = new HashMap<>();
            format.put("noCommas", "true");
            final Column newColumn = new Column(null, "new_col", "new_col", "Description", "text", 3, null, format, "text");
            importer.addColumn(workingCopy.getId(), newColumn);
            importer.updateDatasetInfo(workingCopy);
            final DatasetInfo postPublish = importer.publish(workingCopy.getId());
            final Dataset postPublish2 = (Dataset) importer.loadDatasetInfo(postPublish.getId());

            TestCase.assertEquals(3, postPublish2.getColumns().size());
        } finally {
            importer.deleteDataset(dataset.getId());
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
        TestCase.assertTrue(results2.size() >= 2);

        //
        //  Now delete
        importer.deleteDataset(publishedResults2.getId());

    }

    @Test
    public void testMakingPublic() throws IOException, SodaError, InterruptedException
    {

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        final Dataset newDataset = createPrivateDataset(importer);

        try {
            final DatasetInfo publishedView = importer.publish(newDataset.getId());
            TestCase.assertEquals(null, publishedView.getGrants());

            importer.makePublic(publishedView.getId());
            final Dataset publicDataset = (Dataset) importer.loadDatasetInfo(publishedView.getId());
            TestCase.assertNotNull(publicDataset.getGrants());
            TestCase.assertEquals(1, Collections2.filter(publicDataset.getGrants(), Grant.IS_PUBLIC).size());

            importer.makePrivate(publishedView.getId());
            final Dataset privateDataset = (Dataset) importer.loadDatasetInfo(publishedView.getId());
            TestCase.assertEquals(null, privateDataset.getGrants());
        } finally {
            importer.deleteDataset(newDataset.getId());
        }
    }


    //@Test
    public void testLongRunningOperations() throws IOException, SodaError, InterruptedException
    {
        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);


        final String name = "BigDatasetName" + UUID.randomUUID();

        DatasetInfo di = importer.createViewFromCsv(name, name, CRIMES_CSV, "ID");
        importer.publish(di.getId());
        List ret = consumer.query(di.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(1000, ret.size());


        DatasetInfo workingCopy = importer.createWorkingCopy(di.getId());
        workingCopy = importer.replace(workingCopy.getId(), CRIMES_CSV, 1, null);
        importer.publish(workingCopy.getId());
        ret = consumer.query(di.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(1000, ret.size());


        final Soda2Producer producer = new Soda2Producer(connection);
        producer.upsertCsv(workingCopy.getId(), CRIMES_CSV);
        ret = consumer.query(di.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(1000, ret.size());


        producer.replaceCsv(workingCopy.getId(), CRIMES_CSV);
        ret = consumer.query(di.getId(), SoqlQuery.SELECT_ALL, Soda2Consumer.HASH_RETURN_TYPE);
        TestCase.assertEquals(1000, ret.size());


    }

    @Test
    public void testComments() throws IOException, SodaError, InterruptedException
    {

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);
        final Soda2Consumer consumer = new Soda2Consumer(connection);

        final Dataset newDataset = createPrivateDataset(importer);

        try {
            final DatasetInfo publishedView = importer.publish(newDataset.getId());
            TestCase.assertEquals(null, publishedView.getGrants());


            final Comment comment = new Comment();
            comment.setBody("Hello Kitty");
            comment.setTitle("Hello Title");

            Comment retVal = importer.addComment(publishedView.getId(), comment);
            System.out.println(retVal);

        } finally {
            importer.deleteDataset(newDataset.getId());
        }
    }

    @Test
    public void testAsyncPublish() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();

        final HttpLowLevel connection = connect();
        final SodaImporter importer = new SodaImporter(connection);

        final Dataset view = new Dataset();
        view.setName(name);
        view.setColumns(new ArrayList<Column>());
        view.setFlags(new ArrayList<String>());

        final Dataset createdView = (Dataset) importer.createDataset(view);
        TestCase.assertEquals("unpublished", createdView.getPublicationStage());

        importer.publish(createdView.getId(), true);

        final Dataset updatedView = (Dataset) importer.updateDatasetInfo(createdView);
        TestCase.assertEquals("published", updatedView.getPublicationStage());
    }


    Dataset createPrivateDataset(final SodaImporter importer) throws SodaError, InterruptedException, IOException
    {
        final String name = "Name" + UUID.randomUUID();


        final Dataset view = new Dataset();
        view.setName(name);
        view.setDescription("Hello Kitty");
        view.setTags(Lists.newArrayList("Red", "Blue"));
        HashMap<String, String> format = new HashMap<>();
        format.put("noCommas", "true");
        view.setColumns(Lists.newArrayList(
                new Column(0, "col1", "col1", "col1-desc", "Text", 0, 10, format, "Text"),
                new Column(0, "col2", "col2", "col2-desc", "Text", 0, 10, format, "Text")
        ));
        view.setFlags(new ArrayList<String>());

        return (Dataset) importer.createDataset(view);
    }
}
