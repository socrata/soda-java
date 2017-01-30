package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.socrata.model.importer.DatasetInfo;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment
{

    private DatasetInfo view;
    private Integer parentId;
    private Integer rowId;
    private String  body;
    private String  title;

    @JsonProperty("view")
    public DatasetInfo getView()
    {
        return view;
    }

    @JsonProperty("view")
    public void setView(DatasetInfo view)
    {
        this.view = view;
    }

    @JsonProperty("parentId")
    public Integer getParentId()
    {
        return parentId;
    }

    @JsonProperty("parentId")
    public void setParentId(Integer parentId)
    {
        this.parentId = parentId;
    }

    @JsonProperty("rowId")
    public Integer getRowId()
    {
        return rowId;
    }

    @JsonProperty("rowId")
    public void setRowId(Integer rowId)
    {
        this.rowId = rowId;
    }

    @JsonProperty("body")
    public String getBody()
    {
        return body;
    }

    @JsonProperty("body")
    public void setBody(String body)
    {
        this.body = body;
    }

    @JsonProperty("title")
    public String getTitle()
    {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title)
    {
        this.title = title;
    }
}
