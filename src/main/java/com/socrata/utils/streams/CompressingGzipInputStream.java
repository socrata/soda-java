package com.socrata.utils.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

/**
 */
public class CompressingGzipInputStream extends IdentityStream {

    public CompressingGzipInputStream(InputStream srcStream) throws IOException
    {
        this(4096, srcStream);
    }

    public CompressingGzipInputStream(int bufferSize, InputStream srcStream) throws IOException
    {
        super(bufferSize, srcStream);
        pipedOutputStream = new GZIPOutputStream(pipedOutputStream);
    }
}
