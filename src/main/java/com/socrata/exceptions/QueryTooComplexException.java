package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

/**
 * Thrown if a query is too complicated to run.
 */
public class QueryTooComplexException extends SodaError
{

    public QueryTooComplexException(SodaErrorResponse errorDetail)
    {
        super(errorDetail, 403);
    }
}
