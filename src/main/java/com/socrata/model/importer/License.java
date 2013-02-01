package com.socrata.model.importer;

/**
 * Represents a license for a dataset.
 *
 */
public class License
{
    String name;
    String logoUrl;
    String termsLink;

    public License()
    {
    }

    public License(String name, String logoUrl, String termsLink)
    {
        this.name = name;
        this.logoUrl = logoUrl;
        this.termsLink = termsLink;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        this.logoUrl = logoUrl;
    }

    public String getTermsLink()
    {
        return termsLink;
    }

    public void setTermsLink(String termsLink)
    {
        this.termsLink = termsLink;
    }
}
