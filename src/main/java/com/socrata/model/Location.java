package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location
{
    public final Double longitude;
    public final Double latitude;
    public final Address address;


    @JsonCreator
    public Location(final @JsonProperty("longitude") Double longitude, final @JsonProperty("latitude") Double latitude, final @JsonProperty("human_address") Address address)
    {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    @JsonProperty("longitude")
    public Double getLongitude()
    {
        return longitude;
    }

    @JsonProperty("latitude")
    public Double getLatitude()
    {
        return latitude;
    }

    @JsonProperty("human_address")
    public Address getAddress()
    {
        return address;
    }
}
