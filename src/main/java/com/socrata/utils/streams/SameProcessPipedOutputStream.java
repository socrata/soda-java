package com.socrata.utils.streams;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This is used in concert with SameProcessPipedInputStream to be able to buffer an
 * output stream into an input stream that can be used on the same thread.
 *
 * This is similar to the Java PipedOutputStream, except that this is ONLY safe in
 * a single threaded use, and  PipedOutputStream is NOT safe in a single threaded use
 */
@NotThreadSafe
public class SameProcessPipedOutputStream extends OutputStream {

    final private SameProcessPipedInputStream   reciever;

    public SameProcessPipedOutputStream(SameProcessPipedInputStream reciever) {
        this.reciever = reciever;
    }

    @Override
    public void write(int b) throws IOException {
        reciever.recieveByte(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        reciever.recieveBytes(b, off, len);
    }

    @Override
    public void close() throws IOException {
        reciever.closeForWrite();
    }
}
