package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

import javax.annotation.Nonnull;

/**
 * Thrown if a query is too expensive, and has taken too long to run.
 */
public class QueryTimeoutException extends SodaError
{
    public QueryTimeoutException(@Nonnull final SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse, 408);
    }
}
