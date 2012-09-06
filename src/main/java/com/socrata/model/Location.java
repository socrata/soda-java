package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Location
{
    public final double longitude;
    public final double latitude;
    public final Address address;


    @JsonCreator
    public Location(final @JsonProperty("longitude") double longitude, final @JsonProperty("latitude") double latitude, final @JsonProperty("human_address") Address address)
    {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    @JsonProperty("longitude")
    public double getLongitude()
    {
        return longitude;
    }

    @JsonProperty("latitude")
    public double getLatitude()
    {
        return latitude;
    }

    @JsonProperty("human_address")
    public Address getAddress()
    {
        return address;
    }
}
