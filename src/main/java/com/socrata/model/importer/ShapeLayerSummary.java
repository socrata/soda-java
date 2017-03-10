package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Summary of a shape file layer.
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ShapeLayerSummary
{
    public final String name;
    public final int    featureCount;
    public final int    layerId;
    public final String referenceSystem;

    @JsonCreator
    public ShapeLayerSummary(final @JsonProperty("name")            String name,
                             final @JsonProperty("featureCount")    int featureCount,
                             final @JsonProperty("layerId")         int layerId,
                             final @JsonProperty("referenceSystem") String referenceSystem)
    {
        this.name = name;
        this.featureCount = featureCount;
        this.layerId = layerId;
        this.referenceSystem = referenceSystem;
    }

    /**
     * Name of the layer
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Number of features in this layer.
     * @return
     */
    public int getFeatureCount()
    {
        return featureCount;
    }

    /**
     * ID of this layer
     * @return
     */
    public int getLayerId()
    {
        return layerId;
    }

    /**
     * Reference system for this layer.
     * @return
     */
    public String getReferenceSystem()
    {
        return referenceSystem;
    }
}
