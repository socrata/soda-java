package com.socrata;

import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Consumer;
import com.socrata.api.SodaDdl;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.SearchResults;
import com.socrata.model.search.SearchClause;
import com.socrata.model.soql.ConditionalExpression;
import com.socrata.model.soql.SoqlQuery;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import test.model.DataType;
import test.model.ToxinData;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Not quite a unit test that tests the API against a sandbox domain.  This will depend on the TestData dataset, and
 * will test results against known results in that dataset
 */
public class ApiTest extends TestBase
{


    /**
     * Tests a number of ways to connect using 4x4s
     *
     * Will issue a simple query, and do spot validation.
     */
    @Test
    @Ignore // TODO Dataset isn't present, change tests to create dataset
    public void testSimpleQuery4x4() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final HttpLowLevel  connection = connect();
        final SodaDdl       sodaDdl= new SodaDdl(connection);

        final SearchResults results =  sodaDdl.searchViews(new SearchClause.NameSearch("Test Data"));
        final String id = results.getResults().get(0).getDataset().getId();
        executeSimpleQuery(connection, id);
    }



    /**
     * Tests a number of ways to connect using dataset name
     */
    @Test
    @Ignore // TODO Dataset isn't present, change tests to create dataset
    public void testSimpleQueryName() throws LongRunningQueryException, SodaError, InterruptedException, IOException
    {
        final HttpLowLevel connection = connect();
        executeSimpleQuery(connection, "TestData");
    }


    private void executeSimpleQuery(final HttpLowLevel connection, final String dataset) throws LongRunningQueryException, SodaError, InterruptedException
    {

        //Create a query that should return a single result
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression("primary_naics=325510"))
                .build();

        final Soda2Consumer   soda2Consumer = new Soda2Consumer(connection);

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
