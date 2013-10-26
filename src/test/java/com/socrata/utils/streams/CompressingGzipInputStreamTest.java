package com.socrata.utils.streams;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 */
public class CompressingGzipInputStreamTest
{
    public static final String TEST_STIRNG = "1234567890-=QWERTYUIOP{}ASDFGHJKL:ZXCVBNM<>?";

    @Test
    public void testCompression() throws IOException
    {
        InputStream is = new ByteArrayInputStream(TEST_STIRNG.getBytes("UTF-8"));
        CompressingGzipInputStream  compressingGzipInputStreamis = new CompressingGzipInputStream(1000, is);
        GZIPInputStream gzipInputStream = new GZIPInputStream(compressingGzipInputStreamis);
        String retVal = IOUtils.toString(gzipInputStream);
        TestCase.assertEquals(TEST_STIRNG, retVal);
    }


}
