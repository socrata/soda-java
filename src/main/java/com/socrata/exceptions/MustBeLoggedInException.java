package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

import javax.annotation.Nonnull;

/**
 * This exception gets thrown if a user needs to have an authenticated session.
 */
public class MustBeLoggedInException extends SodaError
{
    public MustBeLoggedInException(@Nonnull final SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse, 403);
    }
}
