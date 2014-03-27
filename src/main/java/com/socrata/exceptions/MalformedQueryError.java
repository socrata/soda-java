package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

import javax.annotation.Nonnull;

/**
 * Thrown if a query is built incorrectly.
 */
public class MalformedQueryError extends SodaError
{
    public MalformedQueryError(@Nonnull final SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse, 400);
    }
}
