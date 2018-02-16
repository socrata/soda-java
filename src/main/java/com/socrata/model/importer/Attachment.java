package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Attachment
{
    private String assetId;
    private String name;
    private String filename;

    public Attachment() {
    }

    public Attachment(String assetId, String name, String filename)
    {
        this.assetId = assetId;
        this.name = name;
        this.filename = filename;
    }

    public String getAssetId()
    {
        return assetId;
    }

    public void setAssetId(String assetId)
    {
        this.assetId = assetId;
    }

    /** Use getAssetId instead. */
    @Deprecated
    @JsonIgnore
    public String getBlobId()
    {
        return assetId;
    }

    /** Use setAssetId instead. */
    @Deprecated
    @JsonIgnore
    public void setBlobId(String blobId)
    {
        this.assetId = blobId;
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
