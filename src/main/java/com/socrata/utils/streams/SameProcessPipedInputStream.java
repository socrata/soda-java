package com.socrata.utils.streams;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is used in concert with SameProcessPipedOutputStream to be able to
 * buffer the response of an OutputStream to use as an InputStream elsewhere.
 *
 * This is similar to the Java PipedInputStream, except that this is ONLY safe in
 * a single threaded use, and  PipedInputStream is NOT safe in a single threaded use
 */
@NotThreadSafe
public class SameProcessPipedInputStream extends InputStream {

    boolean             isClosedForWrite = false;
    boolean             isClosedForRead = false;

    ResizableCircularBuffer currReadBuffer;

    public SameProcessPipedInputStream(int bufferSize) {
        currReadBuffer = new ResizableCircularBuffer(bufferSize);
    }

    @Override
    public int available() throws IOException {
        if (isClosedForRead) {
            return 0;
        }
        return currReadBuffer.bytesInBuffer();
    }

    @Override
    public int read() throws IOException {
        if (isClosedForRead) {
            throw new IOException("The input stream is alread closed.  It cannot be read from");
        }

        if (currReadBuffer.isEmpty() && isClosedForWrite) {
            return -1;
        }

        return 0xFF & currReadBuffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isClosedForRead) {
            throw new IOException("The input stream is alread closed.  It cannot be read from");
        }

        if (currReadBuffer.isEmpty() && isClosedForWrite) {
            return -1;
        }

        return currReadBuffer.read(b, off, len);
    }

    public void recieveByte(int b) throws IOException {
        if (isClosedForWrite) {
            throw new IOException("Cannot add bytes to this stream, because it is already closed");
        }

        if (currReadBuffer.isFull()) {
            int newSize = (int) (currReadBuffer.getTotalCapacity() * 1.5);
            currReadBuffer = currReadBuffer.resize(newSize);
        }

        currReadBuffer.write((byte)b);
    }

    public void recieveBytes(byte[] b, int off, int len) throws IOException {

        if (isClosedForWrite) {
            throw new IOException("Cannot add bytes to this stream, because it is already closed") ;
        }

        if (currReadBuffer.bytesLeftToWrite() < len) {
            int newSize = Math.max((int) (currReadBuffer.getTotalCapacity() * 1.5), len);
            currReadBuffer = currReadBuffer.resize(newSize);
        }

        currReadBuffer.write(b, off, len);
    }

    public void closeForWrite() {
        isClosedForWrite = true;
    }

    @Override
    public void close() throws IOException {
        super.close();
        isClosedForRead = true;
    }
}
