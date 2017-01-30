package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a possible location built up from a file scan
 * seeing columns that look like possible parts of a location
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PossibleLocation
{
    final public Integer city;
    final public Integer state;
    final public Integer address;
    final public Integer zip;
    final public Integer latitude;
    final public Integer longitude;

    @JsonCreator
    public PossibleLocation(@JsonProperty(value="address")  Integer address,
                            @JsonProperty(value="city")     Integer city,
                            @JsonProperty(value="state")    Integer state,
                            @JsonProperty(value="zip")      Integer zip,
                            @JsonProperty(value="latitude") Integer latitude,
                            @JsonProperty(value="longitude") Integer longitude)
    {
        this.city = city;
        this.state = state;
        this.address = address;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getCity()
    {
        return city;
    }

    public Integer getState()
    {
        return state;
    }

    public Integer getAddress()
    {
        return address;
    }

    public Integer getZip()
    {
        return zip;
    }

    public Integer getLatitude()
    {
        return latitude;
    }

    public Integer getLongitude()
    {
        return longitude;
    }
}
