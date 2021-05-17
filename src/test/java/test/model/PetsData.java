package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class PetsData
{
    String licenseNumber;
    String name;
    String species;
    String primaryBreed;
    String secondaryBreed;
    String zipCode;

    @JsonCreator
    public PetsData(@JsonProperty("license_number") String licenseNumber,
                     @JsonProperty("name") String name,
                     @JsonProperty("species") String species,
                     @JsonProperty("primary_breed") String primaryBreed,
                     @JsonProperty("secondary_breed") String secondaryBreed,
                     @JsonProperty("zip_code") String zipCode)
    {
        this.licenseNumber = licenseNumber;
        this.name = name;
        this.species = species;
        this.primaryBreed = primaryBreed;
        this.secondaryBreed = secondaryBreed;
        this.zipCode = zipCode;
    }

    public String getLicenseNumber()
    {
        return licenseNumber;
    }

    public String getName()
    {
        return name;
    }

    public String getSpecies()
    {
        return species;
    }

    public String getPrimaryBreed()
    {
        return primaryBreed;
    }

    public String getSecondaryBreed()
    {
        return secondaryBreed;
    }

    public String getZipCode()
    {
        return zipCode;
    }
}
