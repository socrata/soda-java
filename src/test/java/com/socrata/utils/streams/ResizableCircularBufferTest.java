package com.socrata.utils.streams;


import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;

/**
 */
public class ResizableCircularBufferTest {

    public static final byte[] readTestData = new byte[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    };

    public static final int[] chunkSize1 = new int[] {
            10
    };

    public static final int[] chunkSize2 = new int[] {
            1, 3, 4, 7
    };

    public static final int[] chunkSize3 = new int[] {
            7, 5, 3, 1
    };

    public static final int[] chunkSize4 = new int[] {
            9, 1
    };

    public static final int[] chunkSize5 = new int[] {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    public static final int[][] chunkTestCases = new int[][] {
            chunkSize1, chunkSize2, chunkSize3, chunkSize4, chunkSize5
    };




    @Test
    public void simpleReadWriteTests() throws Exception {

        ResizableCircularBuffer currBuffer = new ResizableCircularBuffer(10);

        TestCase.assertEquals(10, currBuffer.getTotalCapacity());
        TestCase.assertTrue(currBuffer.isEmpty());
        TestCase.assertFalse(currBuffer.isFull());

        for (int i=0; i<= currBuffer.getTotalCapacity(); i++) {
            testFullPopulate(currBuffer);

            //Now change the offset the buffer is using under the covers
            currBuffer.write((byte) 0);
            currBuffer.read();
        }
    }

    @Test
    public void arrayReadWriteTests() throws Exception {

        ResizableCircularBuffer currBuffer = new ResizableCircularBuffer(10);

        TestCase.assertEquals(10, currBuffer.getTotalCapacity());
        TestCase.assertTrue(currBuffer.isEmpty());
        TestCase.assertFalse(currBuffer.isFull());

        for (int i=0; i<= currBuffer.getTotalCapacity(); i++) {

            for (int[] chunkTestCase : chunkTestCases) {
                testArrayPopulate(currBuffer, readTestData, chunkTestCase, chunkTestCase);
            }


            //Now change the offset the buffer is using under the covers
            currBuffer.write((byte) 0);
            currBuffer.read();
        }
    }

    @Test
    public void resizeTests() throws Exception {

        ResizableCircularBuffer buffer = new ResizableCircularBuffer(5);
        TestCase.assertEquals(5, buffer.getTotalCapacity());
        TestCase.assertEquals(5, buffer.bytesLeftToWrite());
        TestCase.assertEquals(0, buffer.bytesInBuffer());
        TestCase.assertFalse(buffer.isFull());
        TestCase.assertTrue(buffer.isEmpty());

        buffer = buffer.resize(10);
        TestCase.assertEquals(10, buffer.getTotalCapacity());
        TestCase.assertEquals(10, buffer.bytesLeftToWrite());
        TestCase.assertEquals(0, buffer.bytesInBuffer());
        TestCase.assertFalse(buffer.isFull());
        TestCase.assertTrue(buffer.isEmpty());


        int bytesWritten = buffer.write(readTestData, 0, 5);
        TestCase.assertEquals(5, bytesWritten);

        buffer = buffer.resize(7);
        TestCase.assertEquals(7, buffer.getTotalCapacity());
        TestCase.assertEquals(2, buffer.bytesLeftToWrite());
        TestCase.assertEquals(5, buffer.bytesInBuffer());
        TestCase.assertFalse(buffer.isFull());
        TestCase.assertFalse(buffer.isEmpty());


        buffer = buffer.resize(5);
        TestCase.assertEquals(5, buffer.getTotalCapacity());
        TestCase.assertEquals(0, buffer.bytesLeftToWrite());
        TestCase.assertEquals(5, buffer.bytesInBuffer());
        TestCase.assertTrue(buffer.isFull());
        TestCase.assertFalse(buffer.isEmpty());

        buffer = buffer.resize(10);
        bytesWritten = buffer.write(readTestData, 5, 5);
        TestCase.assertEquals(5, bytesWritten);

        buffer = buffer.resize(10);
        TestCase.assertEquals(10, buffer.getTotalCapacity());
        TestCase.assertEquals(0, buffer.bytesLeftToWrite());
        TestCase.assertEquals(10, buffer.bytesInBuffer());
        TestCase.assertTrue(buffer.isFull());
        TestCase.assertFalse(buffer.isEmpty());

        try {
            buffer = buffer.resize(5);
            TestCase.assertTrue(false);
        } catch (IllegalArgumentException e) {
            //Success
        }

        buffer.read();
        buffer = buffer.resize(10);
        TestCase.assertEquals(10, buffer.getTotalCapacity());
        TestCase.assertEquals(1, buffer.bytesLeftToWrite());
        TestCase.assertEquals(9, buffer.bytesInBuffer());
        TestCase.assertFalse(buffer.isFull());
        TestCase.assertFalse(buffer.isEmpty());

        buffer.read();
        buffer = buffer.resize(8);
        TestCase.assertEquals(8, buffer.getTotalCapacity());
        TestCase.assertEquals(0, buffer.bytesLeftToWrite());
        TestCase.assertEquals(8, buffer.bytesInBuffer());
        TestCase.assertTrue(buffer.isFull());
        TestCase.assertFalse(buffer.isEmpty());

        byte[] junkBuffer = new byte[10];
        buffer.read(junkBuffer, 2, 8);
        buffer = buffer.resize(8);
        TestCase.assertEquals(8, buffer.getTotalCapacity());
        TestCase.assertEquals(8, buffer.bytesLeftToWrite());
        TestCase.assertEquals(0, buffer.bytesInBuffer());
        TestCase.assertFalse(buffer.isFull());
        TestCase.assertTrue(buffer.isEmpty());


    }

    @Test
    public void copyTests() throws Exception {

        ResizableCircularBuffer buffer = new ResizableCircularBuffer(5);
        ResizableCircularBuffer bufferCopy = buffer.copy();
        TestCase.assertEquals(buffer.bytesInBuffer(), bufferCopy.bytesInBuffer());
        TestCase.assertEquals(buffer.getTotalCapacity(), bufferCopy.getTotalCapacity());

        buffer.write(readTestData, 0, 5);
        TestCase.assertFalse(buffer.bytesInBuffer() == bufferCopy.bytesInBuffer());
        TestCase.assertTrue(buffer.getTotalCapacity() == bufferCopy.getTotalCapacity());

        bufferCopy = buffer.copy();
        TestCase.assertEquals(buffer.bytesInBuffer(), bufferCopy.bytesInBuffer());
        TestCase.assertEquals(buffer.getTotalCapacity(), bufferCopy.getTotalCapacity());

        byte[]  buff1 = new byte[5];
        byte[]  buff2 = new byte[5];
        buffer.read(buff1, 0, 5);
        bufferCopy.read(buff2, 0, 5);

        TestCase.assertTrue(Arrays.equals(buff1, buff2));
    }

    @Test
    public void testSomeEdgeCases() {

        ResizableCircularBuffer buffer = new ResizableCircularBuffer(5);

        //Read from empty
        byte[]  buff1 = new byte[5];
        int bytesRead = buffer.read(buff1, 0, 5);
        TestCase.assertEquals(0, bytesRead);

        //Write to full
        int bytesWritten = buffer.write(readTestData, 0, 5);
        TestCase.assertEquals(5, bytesWritten);

        bytesWritten = buffer.write(readTestData, 0, 5);
        TestCase.assertEquals(0, bytesWritten);

        TestCase.assertFalse(buffer.write((byte) 8));

        //Read and write larger than allowed
        bytesRead = buffer.read(buff1, 0, 5000);
        TestCase.assertEquals(5, bytesRead);

        //Read and write larger than allowed
        bytesWritten = buffer.write(readTestData, 0, 500);
        TestCase.assertEquals(5, bytesWritten);

    }


    /**
     * Fully populates a buffer one element at a time, then removes them one at a time.
     *
     * @param currBuffer buffer to test against
     */
    private void testFullPopulate(ResizableCircularBuffer currBuffer) {
        final int totalSize = currBuffer.getTotalCapacity();


        for (int i=0; i<totalSize; i++) {
            TestCase.assertEquals(totalSize-i, currBuffer.bytesLeftToWrite());
            TestCase.assertEquals(i, currBuffer.bytesInBuffer());

            boolean retVal = currBuffer.write((byte) i);
            TestCase.assertEquals(true, retVal);
        }

        TestCase.assertFalse(currBuffer.isEmpty());
        TestCase.assertTrue(currBuffer.isFull());

        TestCase.assertFalse(currBuffer.write((byte)10));

        for (int i=0; i<totalSize; i++) {
            TestCase.assertEquals(totalSize-i, currBuffer.bytesInBuffer());
            TestCase.assertEquals(i, currBuffer.bytesLeftToWrite());

            byte retVal = currBuffer.read();
            TestCase.assertEquals(i, (int)retVal);
        }

        TestCase.assertTrue(currBuffer.isEmpty());
        TestCase.assertFalse(currBuffer.isFull());


    }


    private void testArrayPopulate(ResizableCircularBuffer currBuffer, byte[] inputBuffer, int[] writeChunkSizes, int[] readChunkSizes) {
        final int totalSize = currBuffer.getTotalCapacity();

        int bytesWritten = 0;
        for (final int writeChunkSize : writeChunkSizes) {

            final int bytesLeftInBuffer = (totalSize-bytesWritten);

            TestCase.assertEquals(bytesWritten, currBuffer.bytesInBuffer());
            TestCase.assertEquals(bytesLeftInBuffer, currBuffer.bytesLeftToWrite());

            final int bytesToWrite = Math.min(bytesLeftInBuffer, writeChunkSize);
            final int retVal = currBuffer.write(inputBuffer, bytesWritten, bytesToWrite);
            TestCase.assertEquals(bytesToWrite, retVal);
            bytesWritten += retVal;
        }

        TestCase.assertFalse(currBuffer.isEmpty());
        TestCase.assertTrue(currBuffer.isFull());

        TestCase.assertEquals(0, currBuffer.write(inputBuffer, 0, 1));


        int bytesRead = 0;
        byte[] readBuffer = new byte[totalSize];
        for (final int readChunkSize : readChunkSizes) {

            final int bytesLeftInBuffer = (bytesWritten-bytesRead);

            TestCase.assertEquals(totalSize-bytesRead, currBuffer.bytesInBuffer());
            TestCase.assertEquals(bytesRead, currBuffer.bytesLeftToWrite());

            final int bytesToRead = Math.min(bytesLeftInBuffer, readChunkSize);
            final int retVal = currBuffer.read(readBuffer, bytesRead, readChunkSize);
            TestCase.assertEquals(bytesToRead, retVal);
            bytesRead += retVal;
        }
        TestCase.assertTrue(currBuffer.isEmpty());
        TestCase.assertFalse(currBuffer.isFull());

        TestCase.assertTrue(Arrays.equals(inputBuffer, readBuffer));

        try {
            currBuffer.read();
            TestCase.fail("Should have failed, due to the buffer being empty.");
        } catch (IllegalArgumentException e) {
            //Success
        }
    }
}
