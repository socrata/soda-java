package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A defintion for how to import a particular column in the dataset.
 */
public class BlueprintColumn
{
    private final String name;
    private final String description;
    private final String datatype;

    /**
     * Constructor
     *
     * @param name name of the column to import
     * @param description description of the column to import
     * @param datatype the datatype for the column to be created
     */
    @JsonCreator
    public BlueprintColumn(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("datatype") String datatype)
    {
        this.name = name;
        this.description = description;
        this.datatype = datatype;
    }

    /**
     * name of the column to import
     * @return
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * description of the column to import
     * @return
     */
    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    /**
     * the datatype for the column to be created
     * @return
     */
    @JsonProperty("datatype")
    public String getDatatype()
    {
        return datatype;
    }

}
