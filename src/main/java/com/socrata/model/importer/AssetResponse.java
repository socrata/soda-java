package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

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
