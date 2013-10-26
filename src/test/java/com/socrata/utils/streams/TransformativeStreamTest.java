package com.socrata.utils.streams;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

/**
 */
public class TransformativeStreamTest {

    @Test
    public void testNoElementsToLoad() throws IOException
    {
        StringArrayTransformativeStream idStream = new StringArrayTransformativeStream(10, new String[0]);
        String results = IOUtils.toString(idStream, "utf-8");
        TestCase.assertEquals(0, results.length());
    }

    @Test
    public void testSingleLoad() throws IOException
    {
        StringArrayTransformativeStream idStream = new StringArrayTransformativeStream(10, new String[] { "Hello Kit" });
        String results = IOUtils.toString(idStream, "utf-8");
        TestCase.assertEquals("Hello Kit", results);
    }

    @Test
    public void testSingleLoad2() throws IOException
    {
        StringArrayTransformativeStream idStream = new StringArrayTransformativeStream(10, new String[] { "Hello Kitt" });
        String results = IOUtils.toString(idStream, "utf-8");
        TestCase.assertEquals("Hello Kitt", results);
    }

    @Test
    public void testMultipleLoads() throws IOException
    {
        StringArrayTransformativeStream idStream = new StringArrayTransformativeStream(10, new String[] { "Hello Kitty", "Foo", "Bar", "", "", "END" });
        String results = IOUtils.toString(idStream, "utf-8");
        TestCase.assertEquals("Hello KittyFooBarEND", results);
    }

    @Test
    public void testEmptyEnd() throws IOException
    {
        StringArrayTransformativeStream idStream = new StringArrayTransformativeStream(10, new String[] { "Hello Kitty", "Foo", "Bar", "", "", "END", "" });
        String results = IOUtils.toString(idStream, "utf-8");
        TestCase.assertEquals("Hello KittyFooBarEND", results);
    }


    static public class StringArrayTransformativeStream extends TransformativeStream {

        int         stringIndex = 0;
        String[]    strings;

        public StringArrayTransformativeStream(int bufferSize, String[] strings)
        {
            super(bufferSize);
            this.strings = strings;
        }

        @Override
        protected boolean loadNext() throws IOException
        {

            if (stringIndex >= strings.length) {
                return false;
            } else {
                byte[] curr = strings[stringIndex++].getBytes("utf-8");
                pipedOutputStream.write(curr, 0, curr.length);
                return true;
            }
        }
    }

}
