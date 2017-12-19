package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.ws.rs.core.GenericType;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * This is a java class that represents a White House Nomination.  This will get
 * loaded through the White House Nominations Appointment dataset.
 *
 * This class is built to use the Joda dates.  The test dataset will use Floating
 * Timestamps for nominationDate and Fixed Timestamps for confirmationVoteDate.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NominationWithJoda
{

    public static final GenericType<List<NominationWithJoda>> LIST_TYPE = new GenericType<List<NominationWithJoda>>() {};

    final String        name;
    final String        position;
    final String        agencyName;
    final String        agencyWebsite;
    final LocalDateTime nominationDate;
    final DateTime      confirmationVoteDate;
    final Boolean       confirmed;
    final Boolean       holdover;


    @JsonCreator
    public NominationWithJoda(@JsonProperty("name") String name,
                              @JsonProperty("position") String position,
                              @JsonProperty("agency_name") String agencyName,
                              @JsonProperty("agency_website") String agencyWebsite,
                              @JsonProperty("nomination_date") LocalDateTime nominationDate,
                              @JsonProperty("confirmation_vote") DateTime confirmationVoteDate,
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
    public LocalDateTime getNominationDate()
    {
        return nominationDate;
    }

    @JsonProperty("confirmation_vote")
    public DateTime getConfirmationVoteDate()
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
