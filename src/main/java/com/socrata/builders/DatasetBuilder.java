package com.socrata.builders;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.socrata.model.importer.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the builder model for creating a Dataset.
 *
 * This is useful when creating a dataset from scratch.
 */
public class DatasetBuilder extends AbstractDatasetInfoBuilder<DatasetBuilder, Dataset>
{

    private List<Column>        columns = new ArrayList<Column>();

    public DatasetBuilder()
    {
    }

    public DatasetBuilder(DatasetInfo datasetInfo)
    {
        super(datasetInfo);
        if (datasetInfo instanceof Dataset) {
            List<Column>  columns = ((Dataset)datasetInfo).getColumns();
            List<Column>  translatedColumns = Lists.newArrayList(Collections2.transform(columns, Column.COPY));
            setColumns(translatedColumns);
        }

    }

    public DatasetBuilder setColumns(List<Column> columns)
    {
        this.columns = columns;
        return this;
    }

    public DatasetBuilder addColumn(Column column)
    {
        if (columns == null) {
            columns = Lists.newArrayList();
        }
        columns.add(column);
        return this;
    }

    public DatasetBuilder removeColumn(String columnName)
    {
        if (columns != null) {

            for (Column column : columns) {
                if (columnName.equals(column.getName())) {
                    columns.remove(column);
                    break;
                }
            }
        }

        return this;
    }

    public DatasetBuilder updateColumn(String columnName, Column columnToUpdateTo)
    {
        if (columns != null) {

            int i=0;
            for (Column column : columns) {
                if (columnName.equals(column.getName())) {
                    columns.set(i, columnToUpdateTo);
                    break;
                }
                i++;
            }
        }

        return this;
    }

    public Dataset build() {
        final Dataset retVal = new Dataset();
        populate(retVal);
        retVal.setColumns(columns);
        return retVal;
    }

}
