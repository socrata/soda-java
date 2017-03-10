package com.socrata.utils;

import com.socrata.Resources;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TestGeneralUtils
{

    private static final File NOMINATIONS_CSV = Resources.file("/testNominations.csv");


    @Test
    public void testReadInCsv() throws IOException
    {
        List<Map<String, Object>> objects =  GeneralUtils.readInCsv(NOMINATIONS_CSV);
        TestCase.assertEquals(2, objects.size());
        TestCase.assertEquals("Name, Test", objects.get(0).get("Name"));
        TestCase.assertEquals("Member", objects.get(0).get("Position"));
        TestCase.assertEquals("Testing Board", objects.get(0).get("Agency Name"));
        TestCase.assertEquals("TEST (http://www.test.com)", objects.get(0).get("Agency Website"));
        TestCase.assertEquals("02/14/2011 12:00 AM", objects.get(0).get("Nomination Date"));
        TestCase.assertNull(objects.get(0).get("Confirmation Vote"));
        TestCase.assertNull(objects.get(0).get("Confirmed"));
        TestCase.assertNull(objects.get(0).get("Holdover"));

        TestCase.assertEquals("Kitty, Hello", objects.get(1).get("Name"));
        TestCase.assertEquals("Director of Cute", objects.get(1).get("Position"));
        TestCase.assertEquals("Department of Affection", objects.get(1).get("Agency Name"));
        TestCase.assertEquals("DOA (http://www.doaff.five)", objects.get(1).get("Agency Website"));
        TestCase.assertEquals("10/18/2011 12:00 AM", objects.get(1).get("Nomination Date"));
        TestCase.assertEquals("03/29/2012 12:00 AM", objects.get(1).get("Confirmation Vote"));
        TestCase.assertEquals("true", objects.get(1).get("Confirmed"));
        TestCase.assertNull(objects.get(1).get("Holdover"));

    }
}
