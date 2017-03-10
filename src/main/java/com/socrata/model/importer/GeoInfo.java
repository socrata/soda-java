package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes the shapefile specific information.  The context for this object is
 * normally that it is pulled from a view's metadata.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class GeoInfo
{
    public final String owsUrl;
    public final String bbox;
    public final String layers;
    public final String featureIdAttribute;
    public final String bboxCrs;
    public final String namespace;


    @JsonCreator
    public GeoInfo(@JsonProperty(value="owsUrl") String owsUrl,
                   @JsonProperty(value="bbox")   String bbox,
                   @JsonProperty(value="layers") String layers,
                   @JsonProperty(value="featureIdAttribute") String featureIdAttribute,
                   @JsonProperty(value="bboxCrs") String bboxCrs,
                   @JsonProperty(value="namespace") String namespace)
    {
        this.owsUrl = owsUrl;
        this.bbox = bbox;
        this.layers = layers;
        this.featureIdAttribute = featureIdAttribute;
        this.bboxCrs = bboxCrs;
        this.namespace = namespace;
    }


    /**
     * The URL to the OWS endpoint.
     * @return
     */
    public String getOwsUrl()
    {
        return owsUrl;
    }

    /**
     * The bounding box for this map file.
     * @return
     */
    public String getBbox()
    {
        return bbox;
    }

    public double[] decodeBbox() {
        String[]  bboxParts = (bbox != null) ? bbox.split(",") : null;
        return (bboxParts != null) ? new double[] {Double.parseDouble(bboxParts[0]),
                                                    Double.parseDouble(bboxParts[1]),
                                                    Double.parseDouble(bboxParts[2]),
                                                    Double.parseDouble(bboxParts[3])}
                : null;
    }

    /**
     * A comma delimited list of all the layers in this map
     * @return
     */
    public String getLayers()
    {
        return layers;
    }

    /**
     * A convenience method that splits the layers out into an array of
     * layer names.
     * @return
     */
    public String[] decodeLayers() {
        return layers != null ? layers.split(",") : new String[0];
    }

    public String getFeatureIdAttribute()
    {
        return featureIdAttribute;
    }

    public String getBboxCrs()
    {
        return bboxCrs;
    }

    public String getNamespace()
    {
        return namespace;
    }
}
