package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This represents an error with some of the data being added through
 * an upsert operation.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class UpsertError
{
    final String error;
    final int    index;
    final String primaryKey;

    @JsonCreator
    public UpsertError(@JsonProperty(value="error") String error,
                       @JsonProperty(value="input_index") int index,
                       @JsonProperty(value="primary_key") String primaryKey)
    {
        this.error = error;
        this.index = index;
        this.primaryKey = primaryKey;
    }

    /**
     * Gets the string describing the error adding or updating this row.
     * @return the string describing the error adding or updating this row
     */
    @JsonProperty(value="error")
    public String getError()
    {
        return error;
    }

    /**
     * The 0-based index of the row in the upsert payload that had this error.
     *
     * @return  The 0-based index of the row in the upsert payload that had this error.
     */
    @JsonProperty(value="input_index")
    public int getIndex()
    {
        return index;
    }

    /**
     * The primary key of the row that had an error getting upserted.
     *
     * @return The primary key of the row that had an error getting upserted.
     */
    @JsonProperty(value="primary_key")
    public String getPrimaryKey()
    {
        return primaryKey;
    }
}
