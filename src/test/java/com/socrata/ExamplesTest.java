package com.socrata;

import com.socrata.api.*;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.model.Meta;
import com.socrata.model.UpsertResult;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.soql.OrderByClause;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.model.soql.SortOrder;
import javax.ws.rs.core.Response;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.Nomination;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 */
public class ExamplesTest extends TestBase
{

    public static final File  NOMINATIONS_CSV = Resources.file("/testNominations.csv");


    @Test
    public void readmeConsumerExamples() throws Exception {

        Soda2Consumer consumer = createConsumer();

        //To get a raw String of the results
        Response response = consumer.query("nominationsCopy", HttpLowLevel.JSON_TYPE, SoqlQuery.SELECT_ALL);
        String payload = response.readEntity(String.class);
        TestCase.assertTrue(payload.length() > 0);

        //Get get this automatcally serialized into a set of Java Beans annotated with Jackson JOSN annotations
        List<Nomination> nominations = consumer.query("nominationsCopy", SoqlQuery.SELECT_ALL, Nomination.LIST_TYPE);
        TestCase.assertTrue(nominations.size() > 0);


        //Create a SoQL query to find the nominations for the Department of State
        SoqlQuery   departmentOfStateQuery = new SoqlQueryBuilder()
                .addSelectPhrase("name")
                .addSelectPhrase("position")
                .addSelectPhrase("nomination_date")
                .setWhereClause("agency_name='Department of State'")
                .addOrderByPhrase(new OrderByClause(SortOrder.Descending, "position"))
                .build();
        nominations = consumer.query("nominationsCopy", departmentOfStateQuery, Nomination.LIST_TYPE);
        TestCase.assertTrue(nominations.size() > 0);
    }

    public static final String NOMINATION_STREAM_TO_ADD = "{\n" +
            "  \"agency_name\" : \"Department of Imagination\",\n" +
            "  \"nomination_date\" : \"2009-07-07T07:00:00.000\",\n" +
            "  \"confirmed\" : true,\n" +
            "  \"position\" : \"Imaginary Friend\",\n" +
            "  \"confirmation_vote\" : \"2009-10-05T14:00:00.000Z\",\n" +
            "  \"name\" : \"Bro, Ney\",\n" +
            "}";

    public static final Nomination NOMINATION_TO_ADD = new Nomination(
            "New, User", "Imaginary Friend", "Department of Imagination", null, new Date(), null, null, null
    );

    public static final Nomination NOMINATION_TO_UPDATE = new Nomination(
            "New, User", "Imaginary Friend", "Department of Imagination", null, new Date(), new Date(), true, null
    );

    @Test
    public void readmeProducerExamples() throws Exception {

        //This is the White Nomination Java Bean, that I want to add
        final Nomination NOMINATION_TO_ADD = new Nomination(
                "New, User", "Imaginary Friend", "Department of Imagination", null, new Date(), null, null, null
        );

        //This is the White Nomination Java Bean, that I want to update to
        final Nomination NOMINATION_TO_UPDATE = new Nomination(
                "New, User", "Imaginary Friend", "Department of Imagination", null, new Date(), new Date(), true, null
        );


        //Get the producer class to allow updates of the data set.
        final Soda2Producer producer = createProducer();

        //Get get this automatically serialized into a set of Java Beans annotated with Jackson JOSN annotations
        Meta nominationAddedMeta = producer.addObject("testupdate", NOMINATION_TO_ADD);

        //Update the nomination
        Meta nominationUpdatedMeta = producer.update("testupdate", nominationAddedMeta.getId(), NOMINATION_TO_UPDATE);

        //Delete the nomination
        producer.delete("testupdate", nominationUpdatedMeta.getId());
    }

    @Test
    public void upsertExamples() throws Exception {

        Soda2Producer producer = createProducer();

        InputStream inputStream = getClass().getResourceAsStream("/testNominations.csv");
        UpsertResult upsertResult = producer.upsertStream("testupdate", HttpLowLevel.CSV_TYPE, inputStream);
        TestCase.assertEquals(2, upsertResult.getRowsCreated());

    }

    @Test
    public void importExample() throws Exception {

        //Create a name with a GUID on the end so we know we are not conflicting with someone else running this.
        final String uniqueName = "Nominations-" + UUID.randomUUID().toString();

        final SodaImporter    importer = createImporter();
        final DatasetInfo     nominationsDataset = importer.createViewFromCsv(uniqueName, "This is a test dataset using samples with the nominations schema", NOMINATIONS_CSV, "Name");
        importer.publish(nominationsDataset.getId());
        //Now the dataset is ready to go!

        importer.deleteDataset(nominationsDataset.getId());
    }
}
