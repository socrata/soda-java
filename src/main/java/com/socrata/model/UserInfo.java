package com.socrata.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class UserInfo
{
    private String id;
    private String displayName;
    private String screenName;


    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    @JsonProperty("displayName")
    public String getDisplayName()
    {
        return displayName;
    }

    @JsonProperty("displayName")
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    @JsonProperty("screenName")
    public String getScreenName()
    {
        return screenName;
    }

    @JsonProperty("screenName")
    public void setScreenName(String screenName)
    {
        this.screenName = screenName;
    }
}
