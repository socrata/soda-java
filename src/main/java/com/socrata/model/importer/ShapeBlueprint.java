package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

/**
 * A blueprint to use for creating a View around a shape.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ShapeBlueprint
{
    public final List<ShapeLayer> layers;


    /**
     * Create a blueprint from the results of a Shape file scan.
     *
     * @param scanResults results from calling a scan of a shapefile
     * @return a blueprint that can be used for creating a view based on a shapefile
     */
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

    /**
     * List of the layers to import
     * @return List of the layers to import
     */
    public List<ShapeLayer> getLayers()
    {
        return layers;
    }
}
