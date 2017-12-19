package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.ws.rs.core.GenericType;

import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Nomination)) return false;
        final Nomination that = (Nomination) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(position, that.position) &&
                Objects.equals(agencyName, that.agencyName) &&
                Objects.equals(agencyWebsite, that.agencyWebsite) &&
                Objects.equals(nominationDate, that.nominationDate) &&
                Objects.equals(confirmationVoteDate, that.confirmationVoteDate) &&
                Objects.equals(confirmed, that.confirmed) &&
                Objects.equals(holdover, that.holdover);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, position, agencyName, agencyWebsite, nominationDate, confirmationVoteDate, confirmed, holdover);
    }

    @Override
    public String toString() {
        return "Nomination{" +
                "name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", agencyName='" + agencyName + '\'' +
                ", agencyWebsite='" + agencyWebsite + '\'' +
                ", nominationDate=" + nominationDate +
                ", confirmationVoteDate=" + confirmationVoteDate +
                ", confirmed=" + confirmed +
                ", holdover=" + holdover +
                '}';
    }
}
