package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A layer in the shape file.
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

    /**
     * ID for the layer
     * @return
     */
    public int getLayerId()
    {
        return layerId;
    }

    /**
     * Name of the layer
     * @return
     */
    public String getName()
    {
        return name;
    }
}
