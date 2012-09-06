package com.socrata.builders;

import com.google.common.collect.Lists;
import com.socrata.model.soql.ConditionalExpression;
import com.socrata.model.soql.Expression;
import com.socrata.model.soql.OrderByClause;
import com.socrata.model.soql.SoqlQuery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Builder class for creating SoQL Queries.  Since SoqlQuery is immutable, this object is for creating
 * the SoqlQuery objects.
 */
public class SoqlQueryBuilder
{

    private final List<String> selectClause;
    private final List<String> groupByClause;
    private final List<OrderByClause> orderByClause;

    private Expression havingClause;
    private Expression whereClause;
    private String fullTextSearchClause;
    private Integer offset;
    private Integer limit;

    public SoqlQueryBuilder()
    {
        selectClause = Lists.newArrayList();
        groupByClause = Lists.newArrayList();
        orderByClause = Lists.newArrayList();
    }

    public SoqlQueryBuilder(final SoqlQuery soqlQuery) {
        selectClause = (soqlQuery.selectClause != null) ?  Lists.newArrayList(soqlQuery.selectClause) : new ArrayList<String>();
        groupByClause = (soqlQuery.groupByClause != null) ? Lists.newArrayList(soqlQuery.groupByClause) : new ArrayList<String>();
        orderByClause = (soqlQuery.orderByClause != null) ? Lists.newArrayList(soqlQuery.orderByClause) : new ArrayList<OrderByClause>();
        havingClause = soqlQuery.havingClause;
        whereClause = soqlQuery.whereClause;
        fullTextSearchClause = soqlQuery.fullTextSearchClause;
        offset = soqlQuery.offset;
        limit = soqlQuery.limit;
    }


    @Nonnull
    public SoqlQueryBuilder setSelectPhrase(@Nonnull final List<String> phrases)
    {
        return setList(selectClause, phrases);
    }

    @Nonnull
    public SoqlQueryBuilder addSelectPhrase(@Nonnull final String newValue)
    {
        return addToList(selectClause, newValue);
    }

    @Nonnull
    public SoqlQueryBuilder addSelectPhrases(@Nonnull final Collection<String> newValues)
    {
        return addAllToList(selectClause, newValues);
    }

    @Nonnull
    public SoqlQueryBuilder setGroupByPhrase(@Nonnull final List<String> phrases)
    {
        return setList(groupByClause, phrases);
    }

    @Nonnull
    public SoqlQueryBuilder addGroupByPhrase(@Nonnull final String newValue)
    {
        return addToList(groupByClause, newValue);
    }

    @Nonnull
    public SoqlQueryBuilder addGroupByPhrases(@Nonnull final Collection<String> newValues)
    {
        return addAllToList(groupByClause, newValues);
    }

    @Nonnull
    public SoqlQueryBuilder setOrderByPhrase(@Nonnull final List<OrderByClause> phrases)
    {
        return setList(orderByClause, phrases);
    }

    @Nonnull
    public SoqlQueryBuilder addOrderByPhrase(@Nonnull final OrderByClause newValue)
    {
        return addToList(orderByClause, newValue);
    }

    @Nonnull
    public SoqlQueryBuilder addOrderByPhrases(@Nonnull final Collection<OrderByClause> newValues)
    {
        return addAllToList(orderByClause, newValues);
    }


    @Nonnull
    public SoqlQueryBuilder setHavingClause(@Nullable final Expression havingClause)
    {
        this.havingClause = havingClause;
        return this;
    }

    @Nonnull
    public SoqlQueryBuilder setWhereClause(@Nullable final Expression whereClause)
    {
        this.whereClause = whereClause;
        return this;
    }

    @Nonnull
    public SoqlQueryBuilder setWhereClause(@Nullable final String whereClause)
    {
        this.whereClause = new ConditionalExpression(whereClause);
        return this;
    }


    @Nonnull
    public SoqlQueryBuilder setFullTextSearchClause(@Nullable final String fullTextSearchClause)
    {
        this.fullTextSearchClause = fullTextSearchClause;
        return this;
    }

    @Nonnull
    public SoqlQueryBuilder setOffset(@Nullable final Integer offset)
    {
        this.offset = offset;
        return this;
    }

    @Nonnull
    public SoqlQueryBuilder setLimit(@Nullable final Integer limit)
    {
        this.limit = limit;
        return this;
    }

    public SoqlQuery build() {
        return new SoqlQuery(selectClause.isEmpty() ? null : selectClause,
                             whereClause,
                             groupByClause.isEmpty()? null : groupByClause,
                             havingClause,
                             orderByClause.isEmpty()? null : orderByClause,
                             fullTextSearchClause,
                             offset,
                             limit);
    }


    private final <E> SoqlQueryBuilder setList(final List<E> existingList, final Collection<E> newValues)
    {
        existingList.clear();
        existingList.addAll(newValues);
        return this;
    }

    private final <E> SoqlQueryBuilder addToList(final List<E> existingList, final E newValue)
    {
        existingList.add(newValue);
        return this;
    }

    private final <E> SoqlQueryBuilder addAllToList(final List<E> existingList, final Collection<E> newValues)
    {
        existingList.addAll(newValues);
        return this;
    }


}
