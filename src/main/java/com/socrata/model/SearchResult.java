package com.socrata.model;

import com.google.common.base.Function;
import com.socrata.model.importer.Dataset;
import com.socrata.model.importer.DatasetInfo;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nullable;

/**
 * Returns a single result from a search.
 */
public class SearchResult
{
    public static final Function<SearchResult, DatasetInfo> TO_DATASET = new Function<SearchResult, DatasetInfo>()
    { public DatasetInfo apply(@Nullable SearchResult input) { return  input!=null ? input.getDataset() : null; } };

    final int     totalRows;
    final DatasetInfo dataset;
    final String[] rows;

    @JsonCreator
    public SearchResult(@JsonProperty(value = "totalRows") int totalRows, @JsonProperty(value = "view") DatasetInfo dataset, @JsonProperty(value = "rows") String[] rows)
    {
        this.totalRows = totalRows;
        this.dataset = dataset;
        this.rows = rows;
    }

    @JsonProperty(value = "view")
    public DatasetInfo getDataset()
    {
        return dataset;
    }
}
