package com.socrata.model;

import com.socrata.model.importer.Column;
import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class ColumnTest
{

    ObjectMapper mapper = new ObjectMapper();

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
    
    @Test
    public void testSerializationCompleteColumn() throws Exception
    {
        Column col =  mapper.readValue(JSON_COMPLETE, Column.class);
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

        String roundTripJson = mapper.writeValueAsString(col);
        TestCase.assertEquals(roundTripJson, JSON_COMPLETE);
    }

    @Test
    public void testSerializationIncompleteColumn() throws Exception
    {
        Column col =  mapper.readValue(JSON_INCOMPLETE, Column.class);
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
        Column col =  mapper.readValue(JSON_OVERCOMPLETE, Column.class);
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
