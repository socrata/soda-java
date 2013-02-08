package com.socrata.builders;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.socrata.model.importer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * THis class is useful for creating new datasets.
 *
 */
public class DatasetBuilder
{

    private String              attribution;
    private String              attributionLink;
    private String              category;
    private String              description;
    private String              externalId;
    private String              id;
    private License             license;
    private String              licenseId;
    private String              name;
    private Metadata            metadata;
    private Map<String, Object> privateMetadata;
    private String              publicationStage;
    private List<String>        tags = new ArrayList<String>();
    private List<Column>        columns = new ArrayList<Column>();

    public DatasetBuilder()
    {
    }

    public DatasetBuilder(DatasetInfo datasetInfo)
    {

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

        if (datasetInfo instanceof Dataset) {
            List<Column>  columns = ((Dataset)datasetInfo).getColumns();
            List<Column>  translatedColumns = Lists.newArrayList(Collections2.transform(columns, Column.COPY));
            setColumns(translatedColumns);
        }

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

    public DatasetBuilder setAttribution(String attribution)
    {
        this.attribution = attribution;
        return this;
    }

    public DatasetBuilder setAttributionLink(String attributionLink)
    {
        this.attributionLink = attributionLink;
        return this;
    }

    public DatasetBuilder setCategory(String category)
    {
        this.category = category;
        return this;
    }

    public DatasetBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public DatasetBuilder setExternalId(String externalId)
    {
        this.externalId = externalId;
        return this;
    }

    public DatasetBuilder setId(String id)
    {
        this.id = id;
        return this;
    }

    public DatasetBuilder setLicense(License license)
    {
        this.license = license;
        return this;
    }

    public DatasetBuilder setLicenseId(String licenseId)
    {
        this.licenseId = licenseId;
        return this;
    }

    public DatasetBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    public DatasetBuilder setMetadata(Metadata metadata)
    {
        this.metadata = metadata;
        return this;
    }

    public DatasetBuilder setPrivateMetadata(Map<String, Object> privateMetadata)
    {
        this.privateMetadata = privateMetadata;
        return this;
    }

    public DatasetBuilder setPublicationStage(String publicationStage)
    {
        this.publicationStage = publicationStage;
        return this;
    }

    public DatasetBuilder setTags(List<String> tags)
    {
        this.tags = tags;
        return this;
    }

    public DatasetBuilder addTag(String tag)
    {
        if (tags == null) {
            tags = Lists.newArrayList();
        }
        tags.add(tag);
        return this;
    }

    public DatasetBuilder removeTag(String tag)
    {
        if (tags != null) {
            tags.remove(tag);
        }
        return this;
    }


    public DatasetBuilder setColumns(List<Column> columns)
    {
        this.columns = columns;
        return this;
    }

    public DatasetBuilder addColumn(Column column)
    {
        if (columns == null) {
            columns = Lists.newArrayList();
        }
        columns.add(column);
        return this;
    }

    public DatasetBuilder removeColumn(String columnName)
    {
        if (columns != null) {

            for (Column column : columns) {
                if (columnName.equals(column.getName())) {
                    columns.remove(column);
                    break;
                }
            }
        }

        return this;
    }

    public DatasetBuilder updateColumn(String columnName, Column columnToUpdateTo)
    {
        if (columns != null) {

            int i=0;
            for (Column column : columns) {
                if (columnName.equals(column.getName())) {
                    columns.set(i, columnToUpdateTo);
                    break;
                }
                i++;
            }
        }

        return this;
    }

    public Dataset build() {
        final Dataset retVal = new Dataset();
        retVal.setAttribution(attribution);
        retVal.setAttributionLink(attributionLink);
        retVal.setCategory(category);
        retVal.setDescription(description);
        retVal.setExternalId(externalId);
        retVal.setId(id);
        retVal.setLicense(license);
        retVal.setLicenseId(licenseId);
        retVal.setName(name);
        retVal.setMetadata(metadata);
        retVal.setPrivateMetadata(privateMetadata);
        retVal.setPublicationStage(publicationStage);
        retVal.setTags(tags);
        retVal.setColumns(columns);

        return retVal;
    }

}
