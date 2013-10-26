package com.socrata.utils.streams;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * This is a byte buffer meant for buffering stream input, that is resizable.
 *
 * This will not ever block, but common usage will often assure there is data in the buffer before a read
 */
@NotThreadSafe
public class ResizableCircularBuffer {
    final byte[]  buffer;

    /**
     * Number of total bytes read
     */
    long currRead = 0;

    /**
     * Number of total bytes written
     */
    long currWrite = 0;

    public ResizableCircularBuffer(int buffSize) {
        this.buffer = new byte[buffSize];
    }

    protected ResizableCircularBuffer(byte[] buffer, long currRead, long currWrite) {
        this.buffer = buffer;
        this.currRead = currRead;
        this.currWrite = currWrite;
    }

    final public int getTotalCapacity() {
        return buffer.length;
    }

    final public boolean isEmpty() {
        return currRead == currWrite;
    }

    final public boolean isFull() {
        assert( (currWrite - currRead) <= getTotalCapacity());
        return (currWrite - currRead) == getTotalCapacity();
    }

    final public int bytesLeftToWrite() {
        return getTotalCapacity() - bytesInBuffer();
    }

    final public int bytesInBuffer() {
        assert( (currWrite - currRead) <= getTotalCapacity());
        return (int) (currWrite - currRead);
    }

    final protected int byteWritableToEnd() {
        int readIndex = toIndex(currRead);
        int writeIndex = toIndex(currWrite);

        if (readIndex > writeIndex) {
            return (readIndex-writeIndex);
        } else {
            return buffer.length-writeIndex;
        }
    }

    final protected int bytesReadableToEnd() {
        int readIndex = toIndex(currRead);
        int writeIndex = toIndex(currWrite);

        if (writeIndex > readIndex) {
            return writeIndex-readIndex;
        } else {
            return buffer.length-readIndex;
        }
    }

    public boolean write(byte b) {
        if (isFull()) {
            return false;
        }
        buffer[toIndex(currWrite++)] = b;
        return true;
    }

    public int write(final byte[] bytes, final int off, final int len) {

        if (isFull()) {
            return 0;
        }

        int bytesWritten = 0;
        final int canWriteToEnd = byteWritableToEnd();

        bytesWritten = Math.min(canWriteToEnd, len);
        System.arraycopy(bytes, off, buffer, toIndex(currWrite), bytesWritten);
        currWrite+= bytesWritten;

        if (bytesWritten < len) {
            bytesWritten += write(bytes, off+bytesWritten, len-bytesWritten);
        }

        return bytesWritten;
    }

    public byte read() {
        if (isEmpty()) {
            throw new IllegalArgumentException("Reading from an empty Circular Buffer");
        }
        return buffer[toIndex(currRead++)];
    }

    public int read(byte[] b, int off, int len) {

        if (isEmpty()) {
            return 0;
        }

        final int canReadToEnd = bytesReadableToEnd();
        int bytesToRead = Math.min(canReadToEnd, len);

        int readIndex = (int)(currRead % buffer.length);
        System.arraycopy(buffer, readIndex, b, off, bytesToRead);
        currRead += bytesToRead;

        if (bytesToRead < len) {
            bytesToRead += read(b, off + bytesToRead, len-bytesToRead);
        }
        return bytesToRead;
    }

    public ResizableCircularBuffer resize(int newSize) {
        if (newSize < bytesInBuffer()) {
            throw new IllegalArgumentException("Cannot reduce buffer to be smaller than the bytes that are currently in it.");
        }

        final ResizableCircularBuffer retVal = new ResizableCircularBuffer(newSize);
        long startingRead = currRead;

        //Read the bytes into the new buffer, but then reset the read count,
        //so the bytes don't count as being read.
        final int bytesCopied = read(retVal.buffer, 0, newSize);
        currRead = startingRead;
        assert(bytesCopied == bytesInBuffer());

        retVal.currWrite = bytesCopied;

        return retVal;
    }

    public ResizableCircularBuffer copy() {
        final byte[] newBuffer = new byte[buffer.length];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        return new ResizableCircularBuffer(newBuffer, currRead, currWrite);
    }

    final private int toIndex(long totalOps) { return (int) (totalOps % buffer.length); }

}
