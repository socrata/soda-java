package com.socrata.utils.streams;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a transformative stream that simply copies from a given InputStream
 * and exposes it through this stream.  This class by itself is not so useful,
 * but is meant as a base class for other streams that want to convert an output
 * stream to an input stream.
 */
public class IdentityStream extends TransformativeStream
{
    final private   byte[] intermediateBuffer = new byte[1024];
    final protected InputStream srcStream;

    public IdentityStream(int bufferSize, InputStream srcStream)
    {
        super(bufferSize);
        this.srcStream = srcStream;
    }

    @Override
    protected boolean loadNext() throws IOException
    {
        int bytes = srcStream.read(intermediateBuffer);
        if (bytes > 0) {
            pipedOutputStream.write(intermediateBuffer, 0, bytes);
        }

        return bytes != -1;
    }
}
