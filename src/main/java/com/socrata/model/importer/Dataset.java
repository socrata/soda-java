package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  A dataset that has been created.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Dataset
{

    String id;
    String name;
    String description;
    Integer rowIdentifierColumnId;
    Long rowsUpdatedAt;
    String displayType;
    String publicationStage;
    Integer viewCount;
    String viewType;
    //Map<String, Object> metadata;
    Map<String, Object> privateMetadata;
    List<String> flags = new ArrayList<String>();
    List<String> rights = new ArrayList<String>();
    List<String> tags = new ArrayList<String>();
    List<Column> columns = new ArrayList<Column>();


    Metadata metadata;
    String category;
    String externalId;

    String attribution;
    String attributionLink;
    String licenseId;
    License license;

    private static final String CUSTOM_FIELDS_ID = "custom_fields";

    public String getId()
    {
        return id;
    }

    private void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public void setColumns(List<Column> columns)
    {
        this.columns = columns;
    }

    public Long getRowsUpdatedAt()
    {
        return rowsUpdatedAt;
    }

    private void setRowsUpdatedAt(Long rowsUpdatedAt)
    {
        this.rowsUpdatedAt = rowsUpdatedAt;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDisplayType()
    {
        return displayType;
    }

    public void setDisplayType(String displayType)
    {
        this.displayType = displayType;
    }

    public String getPublicationStage()
    {
        return publicationStage;
    }

    public void setPublicationStage(String publicationStage)
    {
        this.publicationStage = publicationStage;
    }

    public Integer getViewCount()
    {
        return viewCount;
    }

    private void setViewCount(Integer viewCount)
    {
        this.viewCount = viewCount;
    }

    public String getViewType()
    {
        return viewType;
    }

    public void setViewType(String viewType)
    {
        this.viewType = viewType;
    }

    public List<String> getFlags()
    {
        return flags;
    }

    public void setFlags(List<String> flags)
    {
        this.flags = flags;
    }

    public List<String> getRights()
    {
        return rights;
    }

    public void setRights(List<String> rights)
    {
        this.rights = rights;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Metadata metadata)
    {
        this.metadata = metadata;
    }

    public Map<String, Object> getPrivateMetadata()
    {
        return privateMetadata;
    }

    public void setPrivateMetadata(Map<String, Object> privateMetadata)
    {
        this.privateMetadata = privateMetadata;
    }

    public Integer getRowIdentifierColumnId()
    {
        return rowIdentifierColumnId;
    }

    public void setRowIdentifierColumnId(Integer rowIdentifierColumnId)
    {
        this.rowIdentifierColumnId = rowIdentifierColumnId;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    public String getAttribution()
    {
        return attribution;
    }

    public void setAttribution(String attribution)
    {
        this.attribution = attribution;
    }

    public String getAttributionLink()
    {
        return attributionLink;
    }

    public void setAttributionLink(String attributionLink)
    {
        this.attributionLink = attributionLink;
    }

    public String getLicenseId()
    {
        return licenseId;
    }

    public void setLicenseId(String licenseId)
    {
        this.licenseId = licenseId;
    }

    public License getLicense()
    {
        return license;
    }

    public void setLicense(License license)
    {
        this.license = license;
    }
}
