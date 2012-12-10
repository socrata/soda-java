package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 *
 */
public class BlueprintColumn
{
    final String name;
    final String description;
    final String datatype;

    @JsonCreator
    public BlueprintColumn(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("datatype") String datatype)
    {
        this.name = name;
        this.description = description;
        this.datatype = datatype;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    @JsonProperty("datatype")
    public String getDatatype()
    {
        return datatype;
    }

}
