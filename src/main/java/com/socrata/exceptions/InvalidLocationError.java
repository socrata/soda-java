package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

/**
 * Thrown if an invalid location is returned from the server with a 202.
 */
public class InvalidLocationError extends SodaError
{
    public static final String ERROR_CODE = "client.format.invalidLocation";

    public InvalidLocationError(final String locationSent)
    {
        super(new SodaErrorResponse(ERROR_CODE, "Invalid location from server.", locationSent, null));
    }
}
