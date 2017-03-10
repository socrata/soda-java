package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Column returned by the scan.
 */
public class ScanColumn
{
    final public String name;
    final public String suggestion;
    final public int processed;
    final public Map<String, Integer> types;

    /**
     * Constructor
     *
     * @param name name of the column
     * @param suggestion suggested datatyep for the column
     * @param processed number of rows processed to create this set of recommendations
     * @param types Map of different types
     */
    @JsonCreator
    public ScanColumn(@JsonProperty("name") String name, @JsonProperty("suggestion") String suggestion, @JsonProperty("processed") int processed, @JsonProperty("types") Map<String, Integer> types)
    {
        this.name = name;
        this.suggestion = suggestion;
        this.processed = processed;
        this.types = types;
    }

    public String getName()
    {
        return name;
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public int getProcessed()
    {
        return processed;
    }

    public Map<String, Integer> getTypes()
    {
        return types;
    }
}
