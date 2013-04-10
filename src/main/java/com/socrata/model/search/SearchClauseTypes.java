package com.socrata.model.search;

/**
 * Defines the types of Search Clauses
 */
public enum SearchClauseTypes
{
    name        ("name"),
    tags        ("tags"),
    query       ("q"),
    description ("desc"),
    category    ("category"),
    metadata    ("metadata_tag"),
    viewType    ("datasetView");

    public final String queryParam;

    private SearchClauseTypes(String queryParam)
    {
        this.queryParam = queryParam;
    }
}
