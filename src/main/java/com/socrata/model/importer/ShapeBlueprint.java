package com.socrata.model.importer;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ShapeBlueprint
{
    public final List<ShapeLayer> layers;


    public static ShapeBlueprint fromScanResults(final ShapeScanResults scanResults) {

        final ImmutableList.Builder<ShapeLayer> layers = ImmutableList.builder();
        final ShapeScanSummary summary = scanResults != null ? scanResults.getSummary() : null;
        final List<ShapeLayerSummary> layerSummaries =  summary != null ? summary.getLayers() : Collections.EMPTY_LIST;

        for (ShapeLayerSummary layerSummary : layerSummaries) {
            layers.add(new ShapeLayer(layerSummary.getLayerId(), layerSummary.getName()));
        }

        return new ShapeBlueprint(layers.build());
    }

    @JsonCreator
    public ShapeBlueprint(final @JsonProperty("layers") List<ShapeLayer> layers)
    {
        this.layers = layers;
    }

    public List<ShapeLayer> getLayers()
    {
        return layers;
    }
}
