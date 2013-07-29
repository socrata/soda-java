package com.socrata.model.importer;

import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nullable;
import java.util.List;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Column
{
    private static final String[] RESERVED_FIELD_NAMES = { "_id", "_uuid", "_position", "_address" };

    public static final Function<Column, String>   TO_NAME = new Function<Column, String>()
    { @Override public String apply(@Nullable Column input) { return (input != null) ? input.getName() : null; } };

    public static final Function<Column, Column>   COPY = new Function<Column, Column>()
    { @Override public Column apply(@Nullable Column input) { return (input != null) ? new Column(input) : null; } };

    Integer id;
    String name;
    String fieldName;
    String description;
    String dataTypeName;
    List<Object> flags;
    int position;
    Integer width;

    public Column()
    {
    }

    public Column(Integer id, String name, String fieldName, String description, String dataTypeName, int position, Integer width)
    {
        this.id = id;
        this.name = name;
        this.fieldName = fieldName;
        this.description = description;
        this.dataTypeName = dataTypeName;
        this.position = position;
        this.width = width;
    }

    public Column(Column src)
    {
        this.id = src.id;
        this.name = src.name;
        this.fieldName = src.fieldName;
        this.description = src.description;
        this.dataTypeName = src.dataTypeName;
        this.position = src.position;
        this.width = src.width;
    }

    public Integer getId()
    {
        return id;
    }

    private void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDataTypeName()
    {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName)
    {
        this.dataTypeName = dataTypeName;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    public Integer getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public List<Object> getFlags()
    {
        return flags;
    }

    public void setFlags(List<Object> flags)
    {
        this.flags = flags;
    }
}
