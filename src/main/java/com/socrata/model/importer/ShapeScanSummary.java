package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ShapeScanSummary
{
    final public long totalFeatureCount;
    final public List<ShapeLayerSummary> layers;

    @JsonCreator
    public ShapeScanSummary(final @JsonProperty("totalFeatureCount") long totalFeatureCount,
                            final @JsonProperty("layers") List<ShapeLayerSummary> layers)
    {
        this.totalFeatureCount = totalFeatureCount;
        this.layers = layers;
    }

    public long getTotalFeatureCount()
    {
        return totalFeatureCount;
    }

    public List<ShapeLayerSummary> getLayers()
    {
        return layers;
    }
}
