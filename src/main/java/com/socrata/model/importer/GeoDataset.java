package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.ws.rs.core.GenericType;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * List of IDs for all the child views of this geo dataset.  Each child view will contain
     * the features for 1 layer.
     *
     * @return
     */
    public List<String> getChildViews()
    {
        return childViews;
    }

    /**
     * @param childViews List of IDs for all the child views of this geo dataset.  Each child view will contain
     * the features for 1 layer.
     */
    public void setChildViews(List<String> childViews)
    {
        this.childViews = childViews;
    }

}
