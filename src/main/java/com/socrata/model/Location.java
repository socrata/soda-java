package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
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
