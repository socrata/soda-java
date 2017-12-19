package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.socrata.model.Location;
import javax.ws.rs.core.GenericType;

import java.util.List;

/**
  */
public class LocationTestClass
{
    public static final GenericType<List<LocationTestClass>> LIST_TYPE = new GenericType<List<LocationTestClass>>() {};


    final int         uniqueid;
    final Location    location;
    final String      name;

    @JsonCreator
    public LocationTestClass(@JsonProperty("uniqueid") int uniqueId, @JsonProperty("location") Location location, @JsonProperty("name") String name)
    {
        this.uniqueid = uniqueId;
        this.location = location;
        this.name = name;
    }


    public int getUniqueid()
    {
        return uniqueid;
    }

    public Location getLocation()
    {
        return location;
    }

    public String getName()
    {
        return name;
    }
}
