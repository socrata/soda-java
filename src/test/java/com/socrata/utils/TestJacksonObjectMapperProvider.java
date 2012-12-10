package com.socrata.utils;

import com.socrata.model.importer.Dataset;
import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.util.Date;

/**
 * Test the JacksonObjectMapperProvider
 *
 */
public class TestJacksonObjectMapperProvider
{

    @Test
    public void testMapperProvider() {

        //
        //Make sure the ObjectMapper is setup correctly
        final JacksonObjectMapperProvider objectMapperProvider = new JacksonObjectMapperProvider();
        final ObjectMapper  mapper = objectMapperProvider.getContext(null);
        TestCase.assertNotNull(mapper);

        //
        //  Make sure dates parse correctly
        Date d = new Date(112, 5, 20, 7, 0);
        final JacksonObjectMapperProvider.SocrataDateFormat socrataDateFormat = (JacksonObjectMapperProvider.SocrataDateFormat) mapper.getDeserializationConfig().getDateFormat();

        TestCase.assertNull(socrataDateFormat.parseAsISO8601("12345", new ParsePosition(0)));
        TestCase.assertEquals(d, socrataDateFormat.parseAsISO8601("2012-6-20T07:00:00", new ParsePosition(0)));
        TestCase.assertEquals(d, socrataDateFormat.parseAsISO8601("2012-6-20T07:00:00.000", new ParsePosition(0)));

        TestCase.assertTrue(d.after(socrataDateFormat.parseAsISO8601("2012-6-20T07:00:00-0300", new ParsePosition(0))));
        TestCase.assertTrue(d.after(socrataDateFormat.parseAsISO8601("2012-6-20T07:00:00.000-0300", new ParsePosition(0))));

        TestCase.assertTrue(d.after(socrataDateFormat.parseAsISO8601("2012-6-20T07:00:00Z", new ParsePosition(0))));
        TestCase.assertTrue(d.after(socrataDateFormat.parseAsISO8601("2012-6-20T07:00:00.000Z", new ParsePosition(0))));

    }

    @Test
    public void testViewMapping() throws IOException
    {
        final JacksonObjectMapperProvider objectMapperProvider = new JacksonObjectMapperProvider();
        final ObjectMapper  mapper = objectMapperProvider.getContext(null);

        Dataset v = mapper.readValue(new File("src/test/resources/view.json"), Dataset.class);
        TestCase.assertNotNull(v);
    }

}
