package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 */
public class ScanColumn
{
    final public String name;
    final public String suggestion;
    final public int processed;
    final public Map<String, Integer> types;

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
