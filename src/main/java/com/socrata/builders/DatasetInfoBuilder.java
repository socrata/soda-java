package com.socrata.builders;

import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;

/**
 * Just builds the DatasetInfo portion of a Dataset, using a builder model.  This is useful
 * when building up the changes to a dataset that need to be made outside the publishing cycle.
 *
 * Since, any of the properties in a DatasetInfo can be upldated on a pulished dataset, objects created
 * through this builder can be updated on the server without requiring a working copy (which can be expensive)
 */
public class DatasetInfoBuilder extends AbstractDatasetInfoBuilder<DatasetInfoBuilder, DatasetInfo>
{
    public DatasetInfoBuilder()
    {
    }

    public DatasetInfoBuilder(DatasetInfo datasetInfo)
    {
        super(datasetInfo);
    }

    @Override
    public DatasetInfo build()
    {
        final DatasetInfo retVal = new Dataset();
        populate(retVal);
        return retVal;
    }
}
