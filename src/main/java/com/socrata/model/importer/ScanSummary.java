package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The summary of the results returned by scanning a CSV.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ScanSummary
{

    final public String summary;
    final public int headers;
    final public List<ScanColumn> columns;
    final public List<PossibleLocation> locations;

    /**
     *
     * @param summary Human readable summary of the scan
     * @param headers Number of rows that the scan believes contains the headers
     * @param columns Results of the scan for each column and their suggested types
     * @param locations the columns that can possibly be part of a location
     */
    @JsonCreator
    public ScanSummary(final @JsonProperty("summary") String summary,
                       final @JsonProperty("headers") int headers,
                       final @JsonProperty("columns") List<ScanColumn> columns,
                       final @JsonProperty("locations") List<PossibleLocation> locations)
    {
        this.summary = summary;
        this.headers = headers;
        this.columns = columns;
        this.locations = locations;
    }

    public String getSummary()
    {
        return summary;
    }

    public int getHeaders()
    {
        return headers;
    }

    public List<ScanColumn> getColumns()
    {
        return columns;
    }

    public List<PossibleLocation> getLocations()
    {
        return locations;
    }
}
