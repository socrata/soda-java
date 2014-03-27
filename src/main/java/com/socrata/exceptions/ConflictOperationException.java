package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

/**
 * An exception thrown if there is a conflict.
 */
public class ConflictOperationException extends SodaError {

    public ConflictOperationException(SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse, 409);
    }
}
