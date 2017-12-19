package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.ws.rs.core.GenericType;

import java.util.List;

/**
 */
public class NominationsWText
{
    public static final GenericType<List<NominationsWText>> LIST_TYPE = new GenericType<List<NominationsWText>>() {};

    final String  name;
    final String  position;
    final String  agencyName;
    final String  agencyWebsite;
    final String  nominationDate;
    final String  confirmationVoteDate;
    final String  confirmed;
    final String  holdover;


    @JsonCreator
    public NominationsWText(@JsonProperty("name") String name,
                      @JsonProperty("position") String position,
                      @JsonProperty("agency_name") String agencyName,
                      @JsonProperty("agency_website") String agencyWebsite,
                      @JsonProperty("nomination_date") String nominationDate,
                      @JsonProperty("confirmation_vote") String confirmationVoteDate,
                      @JsonProperty("confirmed") String confirmed,
                      @JsonProperty("holdover") String holdover)
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
    public String getNominationDate()
    {
        return nominationDate;
    }

    @JsonProperty("confirmation_vote")
    public String getConfirmationVoteDate()
    {
        return confirmationVoteDate;
    }

    public String getConfirmed()
    {
        return confirmed;
    }

    public String getHoldover()
    {
        return holdover;
    }
}
