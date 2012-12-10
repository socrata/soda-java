package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * A blueprint is
 *
 */
public class Blueprint
{

    String name;
    String description;
    int skip;
    List<BlueprintColumn> columns;


    @JsonCreator
    public Blueprint(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("skip") int skip, @JsonProperty("columns") List<BlueprintColumn> columns)
    {
        this.name = name;
        this.description = description;
        this.skip = skip;
        this.columns = columns;
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

    @JsonProperty("skip")
    public int getSkip()
    {
        return skip;
    }

    @JsonProperty("columns")
    public List<BlueprintColumn> getColumns()
    {
        return columns;
    }
}
