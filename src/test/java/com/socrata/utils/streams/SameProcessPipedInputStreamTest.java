package com.socrata.utils.streams;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 */
public class SameProcessPipedInputStreamTest {

    public static final byte[] testBytes = new byte[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    };

    @Test
    public void testWithNoData() throws IOException {

        SameProcessPipedInputStream piper = new SameProcessPipedInputStream(10);

        byte[]  readBuff10 = new byte[10];
        int bytesRead = piper.read(readBuff10);
        TestCase.assertEquals(0, bytesRead);
        TestCase.assertEquals(0, piper.available());


        try {
            piper.read();
            TestCase.fail("Read should have failed, because there is no data in the buffer");
        } catch (IllegalArgumentException e) {
            //Success
        }

        piper.closeForWrite();

        bytesRead = piper.read(readBuff10);
        TestCase.assertEquals(-1, bytesRead);

        TestCase.assertEquals(-1, piper.read());
    }

    @Test
    public void testWithSimpleData() throws IOException {

        SameProcessPipedInputStream piper = new SameProcessPipedInputStream(10);

        //Test single byte read
        piper.recieveByte(0);
        TestCase.assertEquals(1, piper.available());

        int byteRead = piper.read();
        TestCase.assertEquals(0, byteRead);

        //Test byte buffer read
        piper.recieveBytes(testBytes, 0, testBytes.length);
        TestCase.assertEquals(testBytes.length, piper.available());

        byte[]  readBuff10 = new byte[10];
        int bytesRead = piper.read(readBuff10);
        TestCase.assertEquals(10, bytesRead);
        TestCase.assertTrue(Arrays.equals(testBytes, readBuff10));

        //Test Expansion
        piper.recieveBytes(testBytes, 0, testBytes.length);
        piper.recieveByte(10);

        bytesRead = piper.read(readBuff10);
        TestCase.assertEquals(10, bytesRead);
        TestCase.assertTrue(Arrays.equals(testBytes, readBuff10));

        bytesRead = piper.read(readBuff10);
        TestCase.assertEquals(1, bytesRead);
        TestCase.assertEquals((byte)10, readBuff10[0]);

        //Test Expansion with Arrays
        for (int i=0; i<4; i++) {
            piper.recieveBytes(testBytes, 0, testBytes.length);
        }

        piper.closeForWrite();

        for (int i=0; i<4; i++) {
            bytesRead = piper.read(readBuff10);
            TestCase.assertEquals(10, bytesRead);
            TestCase.assertTrue(Arrays.equals(testBytes, readBuff10));
        }

        TestCase.assertEquals(-1, piper.read());
        TestCase.assertEquals(-1, piper.read(readBuff10));

        try {
            piper.recieveByte(0);
            TestCase.fail("Connection should be closed");
        } catch (IOException e) {
            //Success
        }

        try {
            piper.recieveBytes(testBytes, 0, testBytes.length);
            TestCase.fail("Connection should be closed");
        } catch (IOException e) {
            //Success
        }
    }
}
