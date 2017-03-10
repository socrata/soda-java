package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Returns all the search results from a search.
  */
public class SearchResults
{

    int count;
    String searchType;
    List<SearchResult> results;

    @JsonCreator
    public SearchResults(@JsonProperty("count")  int count, @JsonProperty("searchType") String searchType, @JsonProperty("results") List<SearchResult> results)
    {
        this.count = count;
        this.searchType = searchType;
        this.results = results;
    }

    public int getCount()
    {
        return count;
    }

    public String getSearchType()
    {
        return searchType;
    }

    public List<SearchResult> getResults()
    {
        return results;
    }
}
