package com.socrata.builders;

import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.importer.NonDataFileDataset;

/**
 * Builds a NonDataFileDataset.This should be mainly used for modifying existing NonDataFileDataset,
 * while using hte methods in SodaImporter for creating the initial NonDataFileDataset.
 */
public class NonDataFileDatasetBuilder extends AbstractDatasetInfoBuilder<NonDataFileDatasetBuilder, NonDataFileDataset>
{
    private String blobFilename;
    private long blobFileSize;
    private String blobId;
    private String blobMimeType;

    public NonDataFileDatasetBuilder()
    {
    }

    public NonDataFileDatasetBuilder(NonDataFileDataset datasetInfo)
    {
        super(datasetInfo);
        this.blobFilename = datasetInfo.getBlobFilename();
        this.blobFileSize = datasetInfo.getBlobFileSize();
        this.blobId = datasetInfo.getBlobId();
        this.blobMimeType = datasetInfo.getBlobMimeType();
    }


    public NonDataFileDatasetBuilder setBlobFilename(String blobFilename)
    {
        this.blobFilename = blobFilename;
        return this;
    }

    public NonDataFileDatasetBuilder setBlobFileSize(long blobFileSize)
    {
        this.blobFileSize = blobFileSize;
        return this;
    }

    public NonDataFileDatasetBuilder setBlobId(String blobId)
    {
        this.blobId = blobId;
        return this;
    }

    public NonDataFileDatasetBuilder setBlobMimeType(String blobMimeType)
    {
        this.blobMimeType = blobMimeType;
        return this;
    }

    @Override
    public NonDataFileDataset build()
    {
        final NonDataFileDataset retVal = new NonDataFileDataset();
        populate(retVal);
        return retVal;
    }
}
