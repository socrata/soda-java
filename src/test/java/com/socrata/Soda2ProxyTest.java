package com.socrata;

import com.google.common.collect.Lists;
import com.socrata.TestBase;
import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Consumer;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.UpsertResult;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.soql.ConditionalExpression;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.utils.GeneralUtils;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.Crime;
import test.model.ToxinData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tests that the proxy settings work
 */
public class Soda2ProxyTest extends TestBase
{

    /**
     * Tests that we fail if we try to go through a non-existent proxy
     */
    @Test
    public void testProxy() throws IOException, LongRunningQueryException, InterruptedException, SodaError
    {

        System.setProperty("https.proxyHost", "webcache.mydomain.com");
        System.setProperty("https.proxyPort", "8080");

        final HttpLowLevel connection = connect();

        try {
            executeSimpleQuery(connection, "77tg-nbgd");
            TestCase.fail("webcache.mydomain.com does not exist, so this call should have failed if it was using the set proxy.");
        } catch (ProcessingException e) {
            //Success
        } finally {
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        }
    }

    private void executeSimpleQuery(final HttpLowLevel connection, final String dataset) throws LongRunningQueryException, SodaError, InterruptedException
    {

        //Create a query that should return a single result
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression("primary_naics=325510"))
                .build();

        final Soda2Consumer soda2Consumer = new Soda2Consumer(connection);

        //
        //   Issue query as a full query
        final Response responseFullQuery = soda2Consumer.query(dataset, HttpLowLevel.JSON_TYPE, query.toString());
        final List<ToxinData> resultsFullQuery = responseFullQuery.readEntity(new GenericType<List<ToxinData>>() {});
        TestCase.assertEquals(6, resultsFullQuery.size());
        for (ToxinData toxinData : resultsFullQuery) {
            TestCase.assertEquals(325510L, toxinData.getPrimaryNAICS());
        }


        //
        //   Issue query as a through $where, etc.

        final Response response = soda2Consumer.query(dataset, HttpLowLevel.JSON_TYPE,query);
        final List<ToxinData> results = response.readEntity(new GenericType<List<ToxinData>>() {});
        TestCase.assertEquals(6, results.size());
        for (ToxinData toxinData : results) {
            TestCase.assertEquals(325510L, toxinData.getPrimaryNAICS());
        }

        //
        //  Issue a query and get back maps
        final List responseList = soda2Consumer.query(dataset,query, new GenericType<List<Object>>() {});
        TestCase.assertEquals(6, responseList.size());
        for (Object crimeObject : responseList) {
            Map<String, String> crimeMap = (Map<String, String>) crimeObject;
            TestCase.assertEquals("325510", crimeMap.get("primary_naics"));
        }
    }
}
