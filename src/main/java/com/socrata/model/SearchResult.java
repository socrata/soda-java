package com.socrata.model;

import com.google.common.base.Function;
import com.socrata.model.importer.Dataset;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nullable;

/**
 * Returns a single result from a search.
 */
public class SearchResult
{
    public static final Function<SearchResult, Dataset> TO_DATASET = new Function<SearchResult, Dataset>()
    { public Dataset apply(@Nullable SearchResult input) { return  input!=null ? input.getDataset() : null; } };

    final int     totalRows;
    final Dataset dataset;

    @JsonCreator
    public SearchResult(@JsonProperty(value = "totalRows") int totalRows, @JsonProperty(value = "view") Dataset dataset)
    {
        this.totalRows = totalRows;
        this.dataset = dataset;
    }

    @JsonProperty(value = "view")
    public Dataset getDataset()
    {
        return dataset;
    }
}
