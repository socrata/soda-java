package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class UpsertResult
{

    public final long rowsCreated;
    public final long rowsUpdated;
    public final long rowsDeleted;
    public final List<UpsertError> errors;
    public final Long truthDataVersion;
    public final Long truthDataShapeVersion;

    @JsonCreator
    public UpsertResult(final @JsonProperty("rows_created") long rowsCreated,
                        final @JsonProperty("rows_updated") long rowsUpdated,
                        final @JsonProperty("rows_deleted") long rowsDeleted,
                        final @JsonProperty("errors") List<UpsertError> errors,
                        final @JsonProperty("truth_data_version") Long truthDataVersion,
                        final @JsonProperty("truth_data_shape_version") Long truthDataShapeVersion)
    {
        this.rowsCreated = rowsCreated;
        this.rowsUpdated = rowsUpdated;
        this.rowsDeleted = rowsDeleted;
        this.truthDataVersion = truthDataVersion;
        this.truthDataShapeVersion = truthDataShapeVersion;
        this.errors = errors;
    }

    public UpsertResult(final long rowsCreated,
                        final long rowsUpdated,
                        final long rowsDeleted,
                        final List<UpsertError> errors)
    {
        this(rowsCreated, rowsUpdated, rowsDeleted, errors, null, null);
    }


    public long getRowsCreated()
    {
        return rowsCreated;
    }

    public long getRowsUpdated()
    {
        return rowsUpdated;
    }

    public long getRowsDeleted()
    {
        return rowsDeleted;
    }

    public List<UpsertError> getErrors()
    {
        return errors;
    }

    public long errorCount()
    {
        return (errors != null) ? errors.size() : 0;
    }

    public Long getTruthDataVersion() {
        return truthDataVersion;
    }

    public Long getTruthDataShapeVersion() {
        return truthDataShapeVersion;
    }

}
