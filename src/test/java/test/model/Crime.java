package test.model;

import javax.ws.rs.core.GenericType;

import java.util.Date;
import java.util.List;

/**
 * This is a test Crime datatype.
 */
public class Crime
{

    public static final GenericType<List<Crime>> LIST_TYPE = new GenericType<List<Crime>>() {};


    Integer id;
    String case_number;
    String date;
    String block;
    String iucr;
    String primary_type;
    String description;
    String location_description;
    String arrest;
    String domestic;
    String beat;
    String district;
    String ward;
    String community_area;
    String fbi_code;
    Double x_coordinate;
    Double y_coordinate;
    Integer year;
    String updated_on;
    Double latitude;
    Double longitude;
    String location;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getCase_number()
    {
        return case_number;
    }

    public void setCase_number(String case_number)
    {
        this.case_number = case_number;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getBlock()
    {
        return block;
    }

    public void setBlock(String block)
    {
        this.block = block;
    }

    public String getIucr()
    {
        return iucr;
    }

    public void setIucr(String iucr)
    {
        this.iucr = iucr;
    }

    public String getPrimary_type()
    {
        return primary_type;
    }

    public void setPrimary_type(String primary_type)
    {
        this.primary_type = primary_type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLocation_description()
    {
        return location_description;
    }

    public void setLocation_description(String location_description)
    {
        this.location_description = location_description;
    }

    public String getArrest()
    {
        return arrest;
    }

    public void setArrest(String arrest)
    {
        this.arrest = arrest;
    }

    public String getDomestic()
    {
        return domestic;
    }

    public void setDomestic(String domestic)
    {
        this.domestic = domestic;
    }

    public String getBeat()
    {
        return beat;
    }

    public void setBeat(String beat)
    {
        this.beat = beat;
    }

    public String getDistrict()
    {
        return district;
    }

    public void setDistrict(String district)
    {
        this.district = district;
    }

    public String getWard()
    {
        return ward;
    }

    public void setWard(String ward)
    {
        this.ward = ward;
    }

    public String getCommunity_area()
    {
        return community_area;
    }

    public void setCommunity_area(String community_area)
    {
        this.community_area = community_area;
    }

    public String getFbi_code()
    {
        return fbi_code;
    }

    public void setFbi_code(String fbi_code)
    {
        this.fbi_code = fbi_code;
    }

    public Double getX_coordinate()
    {
        return x_coordinate;
    }

    public void setX_coordinate(Double x_coordinate)
    {
        this.x_coordinate = x_coordinate;
    }

    public Double getY_coordinate()
    {
        return y_coordinate;
    }

    public void setY_coordinate(Double y_coordinate)
    {
        this.y_coordinate = y_coordinate;
    }

    public Integer getYear()
    {
        return year;
    }

    public void setYear(Integer year)
    {
        this.year = year;
    }

    public String getUpdated_on()
    {
        return updated_on;
    }

    public void setUpdated_on(String updated_on)
    {
        this.updated_on = updated_on;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }
}
