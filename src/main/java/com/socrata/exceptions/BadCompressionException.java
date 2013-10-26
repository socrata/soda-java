package com.socrata.exceptions;

/**
 * Exception thrown if there is a problem wrapping a compression stream
 * around the input stream.
 */
public class BadCompressionException extends SodaError
{
    public BadCompressionException(Throwable throwable)
    {
        super(throwable);
    }
}
