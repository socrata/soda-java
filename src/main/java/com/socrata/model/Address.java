package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The address field located in the Location object's JSON instance.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address
{
    protected final String streetAddress;
    protected final String city;
    protected final String state;
    protected final String zip;

    @JsonCreator
    public Address(final @JsonProperty("address") String streetAddress,
                   final @JsonProperty("city") String city,
                   final @JsonProperty("state") String state,
                   final @JsonProperty("zip") String zip)
    {
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    @JsonProperty("address")
    public String getStreetAddress()
    {
        return streetAddress;
    }

    @JsonProperty("city")
    public String getCity()
    {
        return city;
    }

    @JsonProperty("state")
    public String getState()
    {
        return state;
    }

    @JsonProperty("zip")
    public String getZip()
    {
        return zip;
    }
}
