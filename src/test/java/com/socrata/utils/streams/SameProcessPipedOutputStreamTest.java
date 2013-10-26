package com.socrata.utils.streams;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

/**
 */
public class SameProcessPipedOutputStreamTest {

    public static final byte[] testBytes = new byte[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    };

    @Test
    public void testOutputStream() throws IOException {

        SameProcessPipedInputStream     inputStream = new SameProcessPipedInputStream(5);
        SameProcessPipedOutputStream    outputStream = new SameProcessPipedOutputStream(inputStream);


        outputStream.write(0);
        int val = inputStream.read();
        TestCase.assertEquals(0, val);

        outputStream.write(testBytes, 0, testBytes.length);

        byte[] inputBuffer = new byte[10];
        int bytesRead = inputStream.read(inputBuffer);
        TestCase.assertEquals(10, bytesRead);

        bytesRead = inputStream.read(inputBuffer);
        TestCase.assertEquals(0, bytesRead);

        outputStream.close();
        bytesRead = inputStream.read(inputBuffer);
        TestCase.assertEquals(-1, bytesRead);
        TestCase.assertEquals(-1, inputStream.read());

    }

}
