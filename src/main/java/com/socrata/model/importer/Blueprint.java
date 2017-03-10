package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A structure that defines how a CSV should be imported.
 *
 */
public class Blueprint
{

    private final String name;
    private final String description;
    private final int skip;
    private final List<BlueprintColumn> columns;


    /**
     * Constructor.
     *
     * @param name name of the dataset to be created
     * @param description a human readable description of the dataset
     * @param skip number of columns to skip before importing.  This is normally done to skip headers
     * @param columns a list of the column definition to let the server know how to import the columns.
     */
    @JsonCreator
    public Blueprint(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("skip") int skip, @JsonProperty("columns") List<BlueprintColumn> columns)
    {
        this.name = name;
        this.description = description;
        this.skip = skip;
        this.columns = columns;
    }

    /**
     * Name of the dataset to create
     * @return
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Description of the dataset to create
     * @return
     */
    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    /**
     * Number of columns to skip.  This is normally done to skip headers in a csv
     * @return
     */
    @JsonProperty("skip")
    public int getSkip()
    {
        return skip;
    }

    /**
     * List of the column information to use for importing.
     * @return
     */
    @JsonProperty("columns")
    public List<BlueprintColumn> getColumns()
    {
        return columns;
    }
}
