package com.socrata.model.importer;

import com.socrata.model.Location;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ScanSummary
{

    final public String summary;
    final public int headers;
    final public List<ScanColumn> columns;
    final public List<Location> locations;

    @JsonCreator
    public ScanSummary(final @JsonProperty("summary") String summary,
                       final @JsonProperty("headers") int headers,
                       final @JsonProperty("columns") List<ScanColumn> columns,
                       final @JsonProperty("locations") List<Location> locations)
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

    public List<Location> getLocations()
    {
        return locations;
    }
}
