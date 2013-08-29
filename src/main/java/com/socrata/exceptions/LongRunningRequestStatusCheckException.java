package com.socrata.exceptions;

import com.socrata.api.LongRunningRequest;

/**
 * LongRunningRequestStatusCheckException is thrown if there is a non soda error happen after submitting a long running request and before the completion of the request.
 *
 * Depending on the actual error, you may want to resume status check by using the long running request in the exception.
 */
public class LongRunningRequestStatusCheckException extends SodaError
{
    public LongRunningRequestStatusCheckException(final Throwable cause, final LongRunningRequest<?, ?> longRunningRequest)
    {
        super(cause);
        setLongRunningRequest(longRunningRequest);
    }
}
