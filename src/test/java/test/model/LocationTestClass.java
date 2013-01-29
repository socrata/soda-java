package test.model;

import com.socrata.model.Location;
import com.sun.jersey.api.client.GenericType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

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
