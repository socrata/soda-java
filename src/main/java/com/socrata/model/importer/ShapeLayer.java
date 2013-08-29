package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ShapeLayer
{
    final int layerId;
    final String name;

    @JsonCreator
    public ShapeLayer(final @JsonProperty("layerId") int layerId,
                      final @JsonProperty("name") String name)
    {
        this.layerId = layerId;
        this.name = name;
    }

    public int getLayerId()
    {
        return layerId;
    }

    public String getName()
    {
        return name;
    }
}
