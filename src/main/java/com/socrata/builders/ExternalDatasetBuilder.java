package com.socrata.builders;

import com.google.common.base.Preconditions;
import com.socrata.model.importer.ExternalDataset;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds an external dataset that can be added to the server through the operations in SodaDDL for creating
 * or updating a dataset.
 *
 * External dataset references are datasets that are not hosted by Socrata, but where Socrata provides a reference
 * to another dataset.
 */
public class ExternalDatasetBuilder  extends AbstractDatasetInfoBuilder<ExternalDatasetBuilder, ExternalDataset>
{
    public ExternalDatasetBuilder()
    {
    }

    public ExternalDatasetBuilder(ExternalDataset datasetInfo)
    {
        super(datasetInfo);
    }

    /**
     * Sets all the access points for an external dataset.  This MUST be called after the metadata object is alread
     * set on the object.
     *
     * @param accessPoints the access points to the external dataset.
     * @return
     */
    public ExternalDatasetBuilder setAccessPoints(final Map<String, String> accessPoints) {
        Preconditions.checkState(metadata != null, "Setting accessPoints requires metadata to be set first.  Call setMetadata before calling setAccessPoints.");
        metadata.setAccessPoints(accessPoints);
        return this;
    }

    /**
     * Adds a single access point for an external dataset. This MUST be called after the metadata object is alread
     * set on the object.
     *
     * @param type the object type
     * @param url the URLto the object
     * @return
     */
    public ExternalDatasetBuilder addAccessPoint(final String type, final String url) {
        Preconditions.checkState(metadata != null, "Setting accessPoints requires metadata to be set first.  Call setMetadata before calling setAccessPoints.");
        if (metadata.getAccessPoints() == null) {
            metadata.setAccessPoints(new HashMap<String, String>());
        }

        metadata.getAccessPoints().put(type, url);
        return this;
    }

    @Override
    public ExternalDataset build()
    {
        final ExternalDataset retVal = new ExternalDataset();
        populate(retVal);
        return retVal;
    }
}
