package com.socrata;

import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Consumer;
import com.socrata.api.Soda2Producer;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.SodaError;
import com.socrata.model.Meta;
import com.socrata.model.UpsertResult;
import com.socrata.model.soql.OrderByClause;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.model.soql.SortOrder;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.Nomination;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 */
public class ExamplesTest
{

    @Test
    public void readmeConsumerExamples() throws Exception {

        Soda2Consumer consumer = Soda2Consumer.newConsumer("https://sandbox.demo.socrata.com", "testuser@gmail.com", "OpenData", "D8Atrg62F2j017ZTdkMpuZ9vY");

        //To get a raw String of the results
        ClientResponse response = consumer.getHttpLowLevel().query("nominationsCopy", HttpLowLevel.JSON_TYPE, SoqlQuery.SELECT_ALL);
        String payload = response.getEntity(String.class);
        System.out.println(payload);

        //Get get this automatcally serialized into a set of Java Beans annotated with Jackson JOSN annotations
        List<Nomination> nominations = consumer.query("nominationsCopy", SoqlQuery.SELECT_ALL, Nomination.LIST_TYPE);
        TestCase.assertTrue(nominations.size() > 0);
        System.out.println(nominations.size());


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
        System.out.println(nominations.size());
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
        final Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com", "testuser@gmail.com", "OpenData", "D8Atrg62F2j017ZTdkMpuZ9vY");

        //Get get this automatically serialized into a set of Java Beans annotated with Jackson JOSN annotations
        Meta nominationAddedMeta = producer.addObject("testupdate", NOMINATION_TO_ADD);

        //Update the nomination
        Meta nominationUpdatedMeta = producer.update("testupdate", nominationAddedMeta.getId(), NOMINATION_TO_UPDATE);

        //Delete the nomination
        producer.delete("testupdate", nominationUpdatedMeta.getId());
    }

    @Test
    public void upsertExamples() throws Exception {

        Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com", "testuser@gmail.com", "OpenData", "D8Atrg62F2j017ZTdkMpuZ9vY");

        InputStream inputStream = getClass().getResourceAsStream("/testNominations.csv");
        UpsertResult upsertResult = producer.upsertStream("testupdate", HttpLowLevel.CSV_TYPE, inputStream);
        TestCase.assertEquals(2, upsertResult.getRowsCreated());

    }

}
