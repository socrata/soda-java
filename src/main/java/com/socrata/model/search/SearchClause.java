package com.socrata.model.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Clauses for describing search parameters.
 */
public class SearchClause
{
    final private SearchClauseTypes clauseType;
    final private String value;

    public SearchClause(SearchClauseTypes clauseType, String value)
    {
        this.clauseType = clauseType;
        this.value = value;
    }

    public String getQueryParamName()
    {
        return clauseType.queryParam;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        try {
            return clauseType.queryParam + "=" + URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static public class NameSearch extends SearchClause {

        public NameSearch(String name)
        {
            super(SearchClauseTypes.name, name);
        }
    }

    static public class TagSearch extends SearchClause {

        public TagSearch(String name)
        {
            super(SearchClauseTypes.tags, name);
        }
    }

    static public class QuerySearch extends SearchClause {

        public QuerySearch(String name)
        {
            super(SearchClauseTypes.query, name);
        }
    }

    static public class DescriptionSearch extends SearchClause {

        public DescriptionSearch(String name)
        {
            super(SearchClauseTypes.description, name);
        }
    }

    static public class CategorySearch extends SearchClause {

        public CategorySearch(String name)
        {
            super(SearchClauseTypes.category, name);
        }
    }

    static public class MetadataSearch extends SearchClause {

        public MetadataSearch(String metadataCategory, String metadataKey, String metadataValue)
        {
            super(SearchClauseTypes.metadata, metadataCategory + "_" + metadataKey + ":" + metadataValue);
        }
    }

    static public enum ViewType {
        view ("VIEW") ,
        dataset ("DATASET");

        public final String value;

        private ViewType(String value)
        {
            this.value = value;
        }
    }

    static public class ViewTypeSearch extends SearchClause {

        public ViewTypeSearch(ViewType viewType)
        {
            super(SearchClauseTypes.viewType, viewType.value);
        }
    }
}
