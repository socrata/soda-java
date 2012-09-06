package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class UpsertResult
{

    public final long rowsCreated;
    public final long rowsUpdated;
    public final long rowsDeleted;
    public final long errors;
    public final long bySid;
    public final long byRowIdentifier;


    @JsonCreator
    public UpsertResult(final @JsonProperty("Rows Created") long rowsCreated,
                        final @JsonProperty("Rows Updated") long rowsUpdated,
                        final @JsonProperty("Rows Deleted") long rowsDeleted,
                        final @JsonProperty("Errors") long errors,
                        final @JsonProperty("By SID") long bySid,
                        final @JsonProperty("By RowIdentifier") long byRowIdentifier)
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

    public long getErrors()
    {
        return errors;
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
