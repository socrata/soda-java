package com.socrata.exceptions;

import com.socrata.model.SodaErrorResponse;

import javax.annotation.Nonnull;

/**
 * This exception is a special exception that can be thrown sometimes during
 * an import or append operation.  This exception is thrown when the data
 * has been committed, but there was a problem with the metadata.
 *
 * When this is thrown, the data has already been stored, so re-running non-idempotent
 * operations will cause duplicate records.
 */
public class MetadataUpdateError extends SodaError
{
    public MetadataUpdateError(@Nonnull final SodaErrorResponse sodaErrorResponse)
    {
        super(sodaErrorResponse, 400);
    }
}
