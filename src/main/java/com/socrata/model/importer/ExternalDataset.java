package com.socrata.model.importer;

/**
 * An external dataset is a "dataset" that does not directly store data
 * within Socrata, but rather stores a reference to data elsewhere.
 *
 * The reference to the external dataset is stored within the
 * metadata object, in the accessPoints member.
 *
 * To create an ExternalDataset, simply create the ExternalDataset,
 * Set the access points and then save it using the SodaDdl methods (createDataset)
 *
 * The ExternalDatasetBuilder can make this easier.
 */
public class ExternalDataset extends DatasetInfo
{
    public ExternalDataset()
    {
        setViewType(EXTERNAL_TYPE);
    }
}
