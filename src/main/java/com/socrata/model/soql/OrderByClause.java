package com.socrata.model.soql;

import javax.annotation.Nonnull;

/**
 *   Class that describes a sort order.
 *
 */
public final class OrderByClause {

    @Nonnull
    public final String     columnName;

    @Nonnull
    public final SortOrder  sortOrder;

    public OrderByClause(@Nonnull final SortOrder sortOrder, @Nonnull final String columnName) {
        this.sortOrder = sortOrder;
        this.columnName = columnName;
    }

    @Override
    public String toString()
    {
        StringBuilder   retVal = new StringBuilder()
                .append(columnName)
                .append((sortOrder==SortOrder.Ascending) ? " ASC " : " DESC ");

        return retVal.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderByClause that = (OrderByClause) o;

        if (!columnName.equals(that.columnName)) return false;
        if (sortOrder != that.sortOrder) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = columnName.hashCode();
        result = 31 * result + sortOrder.hashCode();
        return result;
    }
}
