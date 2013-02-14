package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

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
    public final long bySid;
    public final long byRowIdentifier;


    @JsonCreator
    public UpsertResult(final @JsonProperty("rows_created") long rowsCreated,
                        final @JsonProperty("rows_updated") long rowsUpdated,
                        final @JsonProperty("rows_deleted") long rowsDeleted,
                        final @JsonProperty("errors") List<UpsertError> errors,
                        final @JsonProperty("by_sid") long bySid,
                        final @JsonProperty("by_rowidentifier") long byRowIdentifier)
    {
        this.rowsCreated = rowsCreated;
        this.rowsUpdated = rowsUpdated;
        this.rowsDeleted = rowsDeleted;
        this.errors = errors;
        this.bySid = bySid;
        this.byRowIdentifier = byRowIdentifier;
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


    public long getBySid()
    {
        return bySid;
    }

    public long getByRowIdentifier()
    {
        return byRowIdentifier;
    }
}
