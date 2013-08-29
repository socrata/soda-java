package com.socrata.model.importer;

import com.sun.jersey.api.client.GenericType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Dataset backed by a map file of some sort (Esri, kml, kmz, etc.)
 *
 * This derives from  NonDataFileDataset, because the uploaded datafile itself is available as a
 * non-data file for download.  However, in addition, there is a separate child view created for each
 * layer in the dataset.
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class GeoDataset extends NonDataFileDataset
{
    public static final GenericType<List<GeoDataset>> LIST_TYPE = new GenericType<List<GeoDataset>>() {};

    private List<String> childViews = new ArrayList<String>();


    public GeoDataset()
    {
        setViewType(GEODATASET_TYPE);
    }

    public List<String> getChildViews()
    {
        return childViews;
    }

    public void setChildViews(List<String> childViews)
    {
        this.childViews = childViews;
    }

}
