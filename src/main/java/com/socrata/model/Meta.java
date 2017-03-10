package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Meta
{
    final String  id;
    final String  updatedMeta;
    final String  createdMeta;
    final Date    updatedAt;
    final Date    createdAt;

    @JsonCreator
    public Meta(final @JsonProperty(":id") String id,
                final @JsonProperty(":updated_meta") String updatedMeta,
                final @JsonProperty(":created_meta") String createdMeta,
                final @JsonProperty(":updated_at") Date updatedAt,
                final @JsonProperty(":created_at") Date createdAt)
    {
        this.id = id;
        this.updatedMeta = updatedMeta;
        this.createdMeta = createdMeta;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public String getId()
    {
        return id;
    }

    public String getUpdatedMeta()
    {
        return updatedMeta;
    }

    public String getCreatedMeta()
    {
        return createdMeta;
    }

    public Date getUpdatedAt()
    {
        return updatedAt;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }
}
