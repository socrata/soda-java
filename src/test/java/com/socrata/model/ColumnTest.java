package com.socrata.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.socrata.model.importer.Column;
import com.socrata.utils.ObjectMapperFactory;
import junit.framework.TestCase;
import org.junit.Test;

public class ColumnTest
{

    private final ObjectReader reader;
    private final ObjectWriter writer;

    public static final String JSON_COMPLETE = "{" +
            "\"dataTypeName\":\"number\"," +
            "\"description\":\"a column\"," +
            "\"fieldName\":\"col1\"," +
            "\"flags\":[\"hidden\"]," +
            "\"format\":{" +
                "\"mask\":\"##-##-##\"," +
                "\"precisionStyle\":\"standard\"," +
                "\"align\":\"left\"," +
                "\"noCommas\":\"true\"" +
            "}," +
            "\"id\":1414729," +
            "\"name\":\"col1\"," +
            "\"position\":1," +
            "\"renderTypeName\":\"number\","+
            "\"width\":100" +
        "}";

    public static final String JSON_INCOMPLETE = "{" +
            "\"dataTypeName\":\"number\", " +
            "\"fieldName\":\"col1\", " +
            "\"name\":\"col1\", " +
            "\"position\":1, " +
            "\"width\":100 " +
        "}";

    public static final String JSON_OVERCOMPLETE =
            JSON_COMPLETE.substring(0, JSON_COMPLETE.lastIndexOf('}')) +
                    ",\"tableColumnId\":1022638," +
                    "\"cachedContents\":{" +
                        "\"non_null\":1," +
                        "\"smallest\":\"2\"" +
                    "}}";

    public ColumnTest() {
        final ObjectMapper mapper = ObjectMapperFactory.create();
        reader = mapper.readerFor(Column.class);
        writer = mapper.writerFor(Column.class);
    }
    
    @Test
    public void testSerializationCompleteColumn() throws Exception
    {
        Column col =  reader.readValue(JSON_COMPLETE);
        TestCase.assertNotNull(col);
        TestCase.assertEquals(col.getId(), (Integer) 1414729);
        TestCase.assertEquals(col.getPosition(), 1);
        TestCase.assertEquals(col.getWidth(), (Integer) 100);
        TestCase.assertEquals(col.getName(), "col1");
        TestCase.assertEquals(col.getFieldName(), "col1");
        TestCase.assertEquals(col.getDataTypeName(), "number");
        TestCase.assertEquals(col.getRenderTypeName(), "number");
        TestCase.assertEquals(col.getFormat().get("mask"), "##-##-##");
        TestCase.assertEquals(col.getFormat().get("precisionStyle"), "standard");
        TestCase.assertEquals(col.getFormat().get("align"), "left");
        TestCase.assertEquals(col.getFormat().get("noCommas"), "true");

        String roundTripJson = writer.writeValueAsString(col);
        TestCase.assertEquals(roundTripJson, JSON_COMPLETE);
    }

    @Test
    public void testSerializationIncompleteColumn() throws Exception
    {
        Column col =  reader.readValue(JSON_INCOMPLETE);
        TestCase.assertNotNull(col);
        TestCase.assertNull(col.getId());
        TestCase.assertEquals(col.getPosition(), 1);
        TestCase.assertEquals(col.getWidth(), (Integer) 100);
        TestCase.assertEquals(col.getName(), "col1");
        TestCase.assertEquals(col.getFieldName(), "col1");
        TestCase.assertEquals(col.getDataTypeName(), "number");
        TestCase.assertNull(col.getRenderTypeName());
        TestCase.assertNull(col.getFormat());
    }

    @Test
    public void testSerializationOvercompleteColumn() throws Exception
    {
        Column col =  reader.readValue(JSON_OVERCOMPLETE);
        TestCase.assertNotNull(col);
        TestCase.assertEquals(col.getId(), (Integer) 1414729);
        TestCase.assertEquals(col.getPosition(), 1);
        TestCase.assertEquals(col.getWidth(), (Integer) 100);
        TestCase.assertEquals(col.getName(), "col1");
        TestCase.assertEquals(col.getFieldName(), "col1");
        TestCase.assertEquals(col.getDataTypeName(), "number");
        TestCase.assertEquals(col.getRenderTypeName(), "number");
        TestCase.assertEquals(col.getFormat().get("mask"), "##-##-##");
        TestCase.assertEquals(col.getFormat().get("precisionStyle"), "standard");
        TestCase.assertEquals(col.getFormat().get("align"), "left");
        TestCase.assertEquals(col.getFormat().get("noCommas"), "true");
    }
}
