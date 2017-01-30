package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.jersey.api.client.GenericType;

import java.util.Date;
import java.util.List;

/**
 * This is a java class that represents a White House Nomination.  This will get
 * loaded through the White House Nominations Appointment dataset.  It will use Java Dates, however,
 * the test dataset will use Floating Timestamps for nominationDate and Fixed Timestamps for confirmationVoteDate.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Nomination
{

    public static final GenericType<List<Nomination>> LIST_TYPE = new GenericType<List<Nomination>>() {};

    final String  name;
    final String  position;
    final String  agencyName;
    final String  agencyWebsite;
    final Date    nominationDate;
    final Date    confirmationVoteDate;
    final Boolean confirmed;
    final Boolean holdover;


    @JsonCreator
    public Nomination(@JsonProperty("name") String name,
                      @JsonProperty("position") String position,
                      @JsonProperty("agency_name") String agencyName,
                      @JsonProperty("agency_website") String agencyWebsite,
                      @JsonProperty("nomination_date") Date nominationDate,
                      @JsonProperty("confirmation_vote") Date confirmationVoteDate,
                      @JsonProperty("confirmed") Boolean confirmed,
                      @JsonProperty("holdover") Boolean holdover)
    {
        this.name = name;
        this.position = position;
        this.agencyName = agencyName;
        this.agencyWebsite = agencyWebsite;
        this.nominationDate = nominationDate;
        this.confirmationVoteDate = confirmationVoteDate;
        this.confirmed = confirmed;
        this.holdover = holdover;
    }

    public String getName()
    {
        return name;
    }

    public String getPosition()
    {
        return position;
    }

    @JsonProperty("agency_name")
    public String getAgencyName()
    {
        return agencyName;
    }

    @JsonProperty("agency_website")
    public String getAgencyWebsite()
    {
        return agencyWebsite;
    }

    @JsonProperty("nomination_date")
    public Date getNominationDate()
    {
        return nominationDate;
    }

    @JsonProperty("confirmation_vote")
    public Date getConfirmationVoteDate()
    {
        return confirmationVoteDate;
    }

    public Boolean getConfirmed()
    {
        return confirmed;
    }

    public Boolean getHoldover()
    {
        return holdover;
    }
}
