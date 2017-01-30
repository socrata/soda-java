package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DeleteRecord
{
    String  rowIdentifier;
    boolean deleted;

    @JsonCreator
    public DeleteRecord(final @JsonProperty(":id") String rowIdentifier, final @JsonProperty(":deleted") boolean deleted)
    {
        this.rowIdentifier = rowIdentifier;
        this.deleted = deleted;
    }

    @JsonProperty(":id")
    public String getRowIdentifier()
    {
        return rowIdentifier;
    }

    @JsonProperty(":deleted")
    public boolean isDeleted()
    {
        return deleted;
    }
}
