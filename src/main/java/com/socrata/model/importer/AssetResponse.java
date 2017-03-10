package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AssetResponse
{
    final public String id;
    final public String nameForOutput;

    @JsonCreator
    public AssetResponse(final @JsonProperty("id") String id, final @JsonProperty("nameForOutput") String nameForOutput)
    {
        this.id = id;
        this.nameForOutput = nameForOutput;
    }

    public String getId()
    {
        return id;
    }

    public String getNameForOutput()
    {
        return nameForOutput;
    }
}
