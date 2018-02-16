package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder(alphabetic=true)
public class Column
{
    private static final String[] RESERVED_FIELD_NAMES = { "_id", "_uuid", "_position", "_address" };

    public static final Function<Column, String>   TO_NAME = new Function<Column, String>()
    { @Override public String apply(@Nullable Column input) { return (input != null) ? input.getName() : null; } };

    public static final Function<Column, Column>   COPY = new Function<Column, Column>()
    { @Override public Column apply(@Nullable Column input) { return (input != null) ? new Column(input) : null; } };

    int position;
    Integer id;
    Integer width;
    String name;
    String fieldName;
    String description;
    String dataTypeName;
    String renderTypeName;
    List<Object> flags;
    Map<String, String> format;
    Map<String, Object> computationStrategy;

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

    public Column(Integer id, String name, String fieldName, String description, String dataTypeName, int position,
                  Integer width, Map<String, String> format, String renderTypeName)
    {
        this.id = id;
        this.name = name;
        this.fieldName = fieldName;
        this.description = description;
        this.dataTypeName = dataTypeName;
        this.position = position;
        this.width = width;
        this.format = format;
        this.renderTypeName = renderTypeName;
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
        this.format = src.format;
        this.renderTypeName = src.renderTypeName;
    }

    public Integer getId() { return id; }

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

    public String getRenderTypeName()
    {
        return renderTypeName;
    }

    public void setRenderTypeName(String renderTypeName) { this.renderTypeName = renderTypeName; }

    public int getPosition() { return position; }

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

    public List<Object> getFlags() { return flags; }

    public void setFlags(List<Object> flags) { this.flags = flags; }

    public Map<String, String> getFormat()
    {
        return format;
    }

    public void setFormat(Map<String, String> format)
    {
        this.format = format;
    }

    public Map<String, Object> getComputationStrategy() {
        return computationStrategy;
    }
}
