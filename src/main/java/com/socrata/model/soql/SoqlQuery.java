package com.socrata.model.soql;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/**
 * represents a SoQL query
 */
@Immutable
public class SoqlQuery
{
    public static final SoqlQuery   SELECT_ALL = new SoqlQuery(null, null, null, null, null, null, null, null);

    public final ImmutableList<String> selectClause;
    public final Expression whereClause;
    public final ImmutableList<String> groupByClause;
    public final Expression havingClause;
    public final ImmutableList<OrderByClause> orderByClause;
    public final String fullTextSearchClause;
    public final Integer offset;
    public final Integer limit;


    public SoqlQuery(@Nullable List<String> selectClause,
                     @Nullable Expression whereClause,
                     @Nullable List<String> groupByClause,
                     @Nullable Expression havingClause,
                     @Nullable List<OrderByClause> orderByClause,
                     @Nullable String fullTextSearchClause,
                     @Nullable Integer offset,
                     @Nullable Integer limit)
    {
        this.selectClause = (selectClause != null) ? ImmutableList.copyOf(selectClause) : null;
        this.groupByClause = (groupByClause != null) ? ImmutableList.copyOf(groupByClause) : null;
        this.orderByClause = (orderByClause != null) ? ImmutableList.copyOf(orderByClause) : null;

        this.whereClause = whereClause;
        this.havingClause = havingClause;

        this.fullTextSearchClause = fullTextSearchClause;
        this.offset = offset;
        this.limit = limit;
    }

    public UriBuilder toSodaUri(final UriBuilder baseUri)
    {

        if (selectClause != null)
        {
            final String selectString = StringUtils.join(selectClause, ", ");
            baseUri.queryParam(SoqlClauses.select.urlParam, selectString);
        }

        if (whereClause != null)
        {
            baseUri.queryParam(SoqlClauses.where.urlParam, whereClause);
        }

        if (groupByClause != null)
        {
            final String groupByString = StringUtils.join(groupByClause, ", ");
            baseUri.queryParam(SoqlClauses.groupBy.urlParam, groupByString);
        }

        if (havingClause != null)
        {
            baseUri.queryParam(SoqlClauses.having.urlParam, havingClause);
        }

        if (fullTextSearchClause != null)
        {
            baseUri.queryParam(SoqlClauses.fullText.urlParam, fullTextSearchClause);
        }

        if (orderByClause != null)
        {
            final String orderByString = StringUtils.join(orderByClause, ", ");
            baseUri.queryParam(SoqlClauses.orderBy.urlParam, orderByString);
        }

        if (offset != null)
        {
            baseUri.queryParam(SoqlClauses.offset.urlParam, offset);
        }

        if (limit != null)
        {
            baseUri.queryParam(SoqlClauses.limit.urlParam, limit);
        }

        return baseUri;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();

        if (selectClause != null)
        {
            builder.append(SoqlClauses.select.soqlKeyword)
                   .append(' ')
                   .append(StringUtils.join(selectClause, ","))
                   .append(' ');
        } else {
            builder.append("select * ");
        }

        if (whereClause != null)
        {
            builder.append(SoqlClauses.where.soqlKeyword)
                   .append(' ')
                   .append(whereClause.toString())
                   .append(' ');
        }

        if (groupByClause != null)
        {
            builder.append(SoqlClauses.groupBy.soqlKeyword)
                   .append(' ')
                   .append(StringUtils.join(groupByClause, ","))
                   .append(' ');
        }

        if (havingClause != null)
        {
            builder.append(SoqlClauses.having.soqlKeyword)
                   .append(' ')
                   .append(havingClause.toString())
                   .append(' ');
        }

        if (fullTextSearchClause != null)
        {
            builder.append(SoqlClauses.fullText.soqlKeyword)
                   .append(' ')
                   .append(fullTextSearchClause)
                   .append(' ');

        }

        if (orderByClause != null)
        {
            builder.append(SoqlClauses.orderBy.soqlKeyword)
                   .append(' ')
                   .append(StringUtils.join(orderByClause, ","))
                   .append(' ');
        }

        if (offset != null)
        {
            builder.append(SoqlClauses.offset.soqlKeyword)
                   .append(' ')
                   .append(offset)
                   .append(' ');
        }

        if (limit != null)
        {
            builder.append(SoqlClauses.limit.soqlKeyword)
                   .append(' ')
                   .append(limit)
                   .append(' ');
        }

        return builder.toString();
    }
}
