package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

/**
 * An exception thrown if the resource does not exist.
 */
public class DoesNotExistException extends SodaError
{

    public DoesNotExistException(SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse, 404);
    }
}
