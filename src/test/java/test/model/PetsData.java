package test.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

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