package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the basic user information that is present in objects such as DatasetInfo.  This will
 * not contain any personal information like email, but has an ID that will allow a caller to look that
 * up (if they have sufficient priveleges)
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo
{
    private String id;
    private String displayName;
    private String screenName;


    /**
     * Unique id for this user
     * @return
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Unique id for this user
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * User's human readable display name
     * @return
     */
    @JsonProperty("displayName")
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * User's human readable display name
     */
    @JsonProperty("displayName")
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * User's human readable screen name
     * @return
     */
    @JsonProperty("screenName")
    public String getScreenName()
    {
        return screenName;
    }

    /**
     * User's human readable screen name
     */
    @JsonProperty("screenName")
    public void setScreenName(String screenName)
    {
        this.screenName = screenName;
    }
}
