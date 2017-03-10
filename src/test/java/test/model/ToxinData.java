package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.socrata.model.Location;

import java.math.BigInteger;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ToxinData
{

    Integer     year;
    String      facilityId;
    String      facilityName;
    String      streetAddress;
    Location    location;
    BigInteger  docCtrlNum;
    long        primaryNAICS;

    @JsonCreator
    public ToxinData(@JsonProperty("year") Integer year,
                     @JsonProperty("tri_facility_id") String facilityId,
                     @JsonProperty("facility_name") String facilityName,
                     @JsonProperty("street_address") String streetAddress,
                     @JsonProperty("location_1") Location location,
                     @JsonProperty("doc_ctrl_num") BigInteger docCtrlNum,
                     @JsonProperty("primary_naics") long primaryNAICS)
    {
        this.year = year;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.streetAddress = streetAddress;
        this.location = location;
        this.docCtrlNum = docCtrlNum;
        this.primaryNAICS = primaryNAICS;
    }

    public Integer getYear()
    {
        return year;
    }

    public String getFacilityId()
    {
        return facilityId;
    }

    public String getFacilityName()
    {
        return facilityName;
    }

    public String getStreetAddress()
    {
        return streetAddress;
    }

    public Location getLocation()
    {
        return location;
    }

    public BigInteger getDocCtrlNum()
    {
        return docCtrlNum;
    }

    public long getPrimaryNAICS()
    {
        return primaryNAICS;
    }
}
