package com.socrata.builders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.socrata.model.importer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A DatasetInfoBuilder that can be used by builders of all the sub-classes.
 *
 * Handles all the common properties.
 */
abstract public class AbstractDatasetInfoBuilder<BUILDER extends AbstractDatasetInfoBuilder, BUILDEE extends DatasetInfo>
{

    protected String              resourceName;
    protected String              viewType;
    protected String              displayType;
    protected String              attribution;
    protected String              attributionLink;
    protected String              category;
    protected String              description;
    protected String              externalId;
    protected String              id;
    protected License             license;
    protected String              licenseId;
    protected String              name;
    protected Metadata            metadata;
    protected Map<String, Object> privateMetadata;
    protected String              publicationStage;
    protected List<String>        tags = new ArrayList<String>();
    protected List<Column>        columns = new ArrayList<Column>();

    public AbstractDatasetInfoBuilder()
    {
    }

    public AbstractDatasetInfoBuilder(DatasetInfo datasetInfo)
    {

        setResourceName(datasetInfo.getResourceName());
        setAttribution(datasetInfo.getAttribution());
        setAttributionLink(datasetInfo.getAttributionLink());
        setCategory(datasetInfo.getCategory());
        setDescription(datasetInfo.getDescription());
        setExternalId(datasetInfo.getExternalId());
        setId(datasetInfo.getId());
        setLicense(datasetInfo.getLicense());
        setLicenseId(datasetInfo.getLicenseId());
        setName(datasetInfo.getName());
        setPublicationStage(datasetInfo.getPublicationStage());

        if (datasetInfo.getMetadata() != null) {
            setMetadata(Metadata.copy(datasetInfo.getMetadata()));
        }


        if (datasetInfo.getTags() != null) {
            setTags(Lists.newArrayList(datasetInfo.getTags()));
        }

        if (datasetInfo.getPrivateMetadata() != null) {
            setPrivateMetadata(Maps.newHashMap(datasetInfo.getPrivateMetadata()));
        }

    }

    public BUILDER setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
        return (BUILDER) this;
    }


    public BUILDER setViewType(String viewType)
    {
        this.viewType = viewType;
        return (BUILDER) this;
    }

    public BUILDER setDisplayType(String displayType)
    {
        this.displayType = displayType;
        return (BUILDER) this;
    }

    public BUILDER setAttribution(String attribution)
    {
        this.attribution = attribution;
        return (BUILDER) this;
    }

    public BUILDER setAttributionLink(String attributionLink)
    {
        this.attributionLink = attributionLink;
        return (BUILDER) this;
    }

    public BUILDER setCategory(String category)
    {
        this.category = category;
        return(BUILDER) this;
    }

    public BUILDER setDescription(String description)
    {
        this.description = description;
        return (BUILDER) this;
    }

    public BUILDER setExternalId(String externalId)
    {
        this.externalId = externalId;
        return (BUILDER) this;
    }

    public BUILDER setId(String id)
    {
        this.id = id;
        return (BUILDER) this;
    }

    public BUILDER setLicense(License license)
    {
        this.license = license;
        return (BUILDER) this;
    }

    public BUILDER setLicenseId(String licenseId)
    {
        this.licenseId = licenseId;
        return (BUILDER) this;
    }

    public BUILDER setName(String name)
    {
        this.name = name;
        return (BUILDER) this;
    }

    public BUILDER setMetadata(Metadata metadata)
    {
        this.metadata = metadata;
        return (BUILDER) this;
    }

    public BUILDER setPrivateMetadata(Map<String, Object> privateMetadata)
    {
        this.privateMetadata = privateMetadata;
        return (BUILDER) this;
    }

    public BUILDER setPublicationStage(String publicationStage)
    {
        this.publicationStage = publicationStage;
        return (BUILDER) this;
    }

    public BUILDER setTags(List<String> tags)
    {
        this.tags = tags;
        return (BUILDER) this;
    }

    public BUILDER addTag(String tag)
    {
        if (tags == null) {
            tags = Lists.newArrayList();
        }
        tags.add(tag);
        return (BUILDER) this;
    }

    public BUILDER removeTag(String tag)
    {
        if (tags != null) {
            tags.remove(tag);
        }
        return (BUILDER) this;
    }


    /**
     * Copies the common properties into the DatasetInfo that is passed in.
     *
     * It is important to note that this has the side effect of modifying the
     * argument in place.
     *
     * @param retVal The value to populate with properties.
     */
    public void populate(DatasetInfo retVal) {
        retVal.setResourceName(resourceName);
        retVal.setAttribution(attribution);
        retVal.setAttributionLink(attributionLink);
        retVal.setCategory(category);
        retVal.setDescription(description);
        retVal.setDisplayType(displayType);
        retVal.setExternalId(externalId);
        retVal.setId(id);
        retVal.setLicense(license);
        retVal.setLicenseId(licenseId);
        retVal.setName(name);
        retVal.setMetadata(metadata);
        retVal.setPrivateMetadata(privateMetadata);
        retVal.setPublicationStage(publicationStage);
        retVal.setTags(tags);
    }

    /**
     * The method that needs to be implemented by all the sub classes to actually
     * be able to build the object being constructed.
     *
     * @return The object built from the parameters in the builder.
     */
    abstract public BUILDEE build();

}
