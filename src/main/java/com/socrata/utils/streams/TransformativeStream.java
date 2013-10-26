package com.socrata.utils.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This abstract class is a stream that implements an InputStream, but provides
 * a "loadNext" method subclasses can use for populating the inputstream
 */
abstract class TransformativeStream extends InputStream {


    protected SameProcessPipedInputStream pipedInputStream;
    protected OutputStream pipedOutputStream;

    private boolean hasMore = true;

    public TransformativeStream (int bufferSize) {

        pipedInputStream = new SameProcessPipedInputStream(bufferSize);
        pipedOutputStream = new SameProcessPipedOutputStream(pipedInputStream);
    }

    @Override
    public int read() throws IOException
    {
        while (hasMore && pipedInputStream.available() == 0) {
            hasMore = doLoad();
        }

        return (pipedInputStream.available() > 0) ? pipedInputStream.read() : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        while (hasMore && pipedInputStream.available() < len) {
            hasMore = doLoad();
        }

        return (pipedInputStream.available() > 0) ? pipedInputStream.read(b, off, len) : -1;
    }

    @Override
    public int available() throws IOException
    {
        while (hasMore && pipedInputStream.available() == 0) {
            hasMore = loadNext();
        }

        return pipedInputStream.available();
    }



    protected boolean doLoad() throws IOException
    {
        boolean retVal = loadNext();
        if (!retVal) {
            pipedOutputStream.close();
        }
        return retVal;
    }


    /**
     * A method to load the next item that needs to be
     * made ready for the input stream. This method should
     * write objects to the pipedOutputStream, so they will
     * be available from clients treating this object as an
     * InputStream
     *
     * @return true if there is possibly more data to add
     *         false if there is definitively no more data to add
     */
    abstract protected boolean loadNext() throws IOException;


    @Override
    public void close() throws IOException
    {
        super.close();
        pipedOutputStream.close();
        pipedInputStream.close();
    }
}

