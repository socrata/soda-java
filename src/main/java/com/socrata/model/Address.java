package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The address field located in the Location object's JSON instance.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Address
{
    public final String streetAddress;
    public final String city;
    public final String state;
    public final String zip;

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
