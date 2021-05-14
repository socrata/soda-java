package com.socrata;

import com.socrata.api.HttpLowLevel;
import com.socrata.api.Soda2Consumer;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.soql.ConditionalExpression;
import com.socrata.model.soql.SoqlQuery;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;
import junit.framework.TestCase;
import org.junit.Test;
import test.model.PetsData;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
            final Properties testProperties = new Properties();
            testProperties.load(getClass().getClassLoader().getResourceAsStream("TestConfig.properties"));
            String id = "m9na-apdz";
            if (testProperties.getProperty(TestBase.URL_PROP).contains("rc")) {
                id = "5vis-936x";
            }
            executeSimpleQuery(connection, id);
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
        // Create a query that should return a single result
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression("primary_breed='Tiffany'"))
                .build();

        final Soda2Consumer soda2Consumer = new Soda2Consumer(connection);

        // Issue query as a full query
        final Response responseFullQuery = soda2Consumer.query(dataset, HttpLowLevel.JSON_TYPE, query.toString());
        final List<PetsData> resultsFullQuery = responseFullQuery.readEntity(new GenericType<List<PetsData>>() {});
        TestCase.assertEquals(1, resultsFullQuery.size());
    }
}
