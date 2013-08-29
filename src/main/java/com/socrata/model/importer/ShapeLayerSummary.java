package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
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

    public String getName()
    {
        return name;
    }

    public int getFeatureCount()
    {
        return featureCount;
    }

    public int getLayerId()
    {
        return layerId;
    }

    public String getReferenceSystem()
    {
        return referenceSystem;
    }
}
