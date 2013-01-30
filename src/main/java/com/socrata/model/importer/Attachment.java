package com.socrata.model.importer;

/**
 * */
public class Attachment
{
    private String blobId;
    private String name;
    private String filename;

    public Attachment() {
    }

    public Attachment(String blobId, String name, String filename)
    {
        this.blobId = blobId;
        this.name = name;
        this.filename = filename;
    }

    public String getBlobId()
    {
        return blobId;
    }

    public void setBlobId(String blobId)
    {
        this.blobId = blobId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }
}
