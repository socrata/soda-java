package com.socrata.model.importer;

/**
 * Represents a non-dataset file that can be hosted by Socrata.  This is normally an uploaded file.
 *
 * To create a NonDataFileDataset use the methods in SodaImporter.
 */
public class NonDataFileDataset extends DatasetInfo
{
    private String blobFilename;
    private long blobFileSize;
    private String blobId;
    private String blobMimeType;

    public NonDataFileDataset()
    {
        setViewType(FILE_TYPE);
    }

    public String getBlobFilename()
    {
        return blobFilename;
    }

    public void setBlobFilename(String blobFilename)
    {
        this.blobFilename = blobFilename;
    }

    public long getBlobFileSize()
    {
        return blobFileSize;
    }

    public void setBlobFileSize(long blobFileSize)
    {
        this.blobFileSize = blobFileSize;
    }

    public String getBlobId()
    {
        return blobId;
    }

    public void setBlobId(String blobId)
    {
        this.blobId = blobId;
    }

    public String getBlobMimeType()
    {
        return blobMimeType;
    }

    public void setBlobMimeType(String blobMimeType)
    {
        this.blobMimeType = blobMimeType;
    }
}
