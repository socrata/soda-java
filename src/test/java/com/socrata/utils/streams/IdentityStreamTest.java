package com.socrata.utils.streams;

import junit.framework.TestCase;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 */
public class IdentityStreamTest
{
    @Test
    public void testEmptyStream() throws IOException
    {
        IdentityStream  is = new IdentityStream(10, new ByteArrayInputStream(new byte[0]));
        TestCase.assertEquals(0, is.available());
        TestCase.assertEquals(-1, is.read());
        TestCase.assertEquals(-1, is.read(new byte[]{0, 0, 0}, 0, 3));
        is.close();

        is = new IdentityStream(10, new ByteArrayInputStream(new byte[0]));
        TestCase.assertEquals(-1, is.read());
        TestCase.assertEquals(-1, is.read(new byte[]{0, 0, 0}, 0, 3));
        TestCase.assertEquals(0, is.available());
        is.close();

        is = new IdentityStream(10, new ByteArrayInputStream(new byte[0]));
        TestCase.assertEquals(-1, is.read(new byte[]{0, 0, 0}, 0, 3));
        TestCase.assertEquals(0, is.available());
        TestCase.assertEquals(-1, is.read());
        is.close();

    }

    @Test
    public void testStream() throws IOException
    {
        byte[] tempBuff = new byte[9];

        IdentityStream  is = new IdentityStream(10, new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
        TestCase.assertEquals(10, is.available());
        TestCase.assertEquals(1, is.read());
        TestCase.assertEquals(9, is.read(tempBuff, 0, 9));
        TestCase.assertTrue(Arrays.equals(new byte[] {2, 3, 4, 5, 6, 7, 8, 9, 10}, tempBuff));
        is.close();
    }


}
