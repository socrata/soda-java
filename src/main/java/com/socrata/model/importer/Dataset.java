package com.socrata.model.importer;

import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  The schema portions of a dataset.  Anything changed in this class
 *  as opposed to it's parent class (DatasetInfo) should use a working copy.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Dataset extends DatasetInfo
{
    public static final GenericType<List<Dataset>> LIST_TYPE = new GenericType<List<Dataset>>() {};

    private Integer rowIdentifierColumnId;
    private List<String> flags = new ArrayList<String>();
    private List<Column> columns = new ArrayList<Column>();

    /**
     * Gets the list of columns in this dataset.
     * @return the list of columns in this dataset.
     */
    public List<Column> getColumns()
    {
        return columns;
    }

    /**
     * Sets the list of columns in this dataset.
     *  @param columns the list of columns in this dataset.
     */
    public void setColumns(final List<Column> columns)
    {
        this.columns = columns;
    }

    /**
     * Flags describing different characteristics about the dataset.  These are mainly used by the system and shouldn't
     * really be changed.
     * @return list of the flags describing this dataset
     */
    public List<String> getFlags()
    {
        return flags;
    }

    /**
     * Sets the flags describing this dataset.  These are mainly used by the system and should not really be persisted
     * back to the server.
     *
     * @param flags the flags describing this dataset.
     */
    public void setFlags(List<String> flags)
    {
        this.flags = flags;
    }


    /**
     * Looks up a column by it's name and uses that to setup the row identifier column.
     *
     * @param columnName name of the column
     */
    public void setupRowIdentifierColumnByName(final String columnName) {

        if (columnName != null) {
            for (Column column : getColumns()) {
                if (columnName.equals(column.getName())) {
                    setupRowIdentifierColumn(column);
                    return;
                }
            }

            final String columnNames = StringUtils.join(Collections2.transform(getColumns(), Column.TO_NAME), ",");
            throw new IllegalArgumentException("No column named " + columnName + " exists for this dataset.  " +
                                                       "Current column names are: " + columnNames);

        } else {
            setupRowIdentifierColumn(null);
        }
    }

    /**
     * Sets a column to be a row identifier for this dataset.  You can think of this as being the
     * same as a primary key.
     *
     * @param column the column to use as the row identifier.  This MUST have the field set.
     *               If {@code null}, this will clear out the row identifier so the dataset will use the system built-in primary key.
     */
    public void setupRowIdentifierColumn(final Column column) {

        if (column != null) {
            if (column.getId() == null) {
                throw new IllegalArgumentException("A column MUST have it's ID set in order to make it a row identifier, " +
                                                           "this ID will be set on the server when the column is created on a dataset.");
            }

            Metadata metadata = getMetadata();
            if (metadata == null) {
                metadata = new Metadata();
                setMetadata(metadata);
            }

            metadata.setRowIdentifier(column.getFieldName());
            rowIdentifierColumnId = column.getId();
        } else {
            rowIdentifierColumnId = null;
            Metadata metadata = getMetadata();
            if (metadata != null) {
                metadata.setRowIdentifier(null);
            }
        }
    }

    /**
     * Gets the ID of the row identifier column (like a primary key)
     * @return the ID of the row identifier column (like a primary key)
     */
    public Integer getRowIdentifierColumnId()
    {
        return rowIdentifierColumnId;
    }

    /**
     * Sets the row identier column ID for the (like a primary key).  It is important to note
     * that when changing this to update the server, you will need to set the row identifier field in the
     * metadata field as well.  Or you can call setRowIdentifierColumn to do this for you.
     *
     * @param rowIdentifierColumnId the id of the row identifier column (like a primary key)
     */
    public void setRowIdentifierColumnId(final Integer rowIdentifierColumnId)
    {
        this.rowIdentifierColumnId = rowIdentifierColumnId;
    }


}
