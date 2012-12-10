package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 */
public class GeocodingResults
{
    private final long total;
    private final long view;

    @JsonCreator
    public GeocodingResults(final @JsonProperty("total") long total,
                            final @JsonProperty("view") long view)
    {
        this.total = total;
        this.view = view;
    }

    @JsonProperty("total")
    public long getTotal()
    {
        return total;
    }

    @JsonProperty("view")
    public long getView()
    {
        return view;
    }

}
