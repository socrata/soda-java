package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Class represents the Metadata for a dataset.
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Metadata
{

    Map<String, Map<String, String>>    custom_metadata;
    List<String>                        warnings;
    String                              rowIdentifier;
    String                              rdfSubject;
    String                              rdfClass;


    public Metadata()
    {
    }

    public Metadata(Map<String, Map<String, String>> custom_metadata, String rowIdentifier, String rdfSubject, String rdfClass)
    {
        this.custom_metadata = custom_metadata;
        this.rowIdentifier = rowIdentifier;
        this.rdfSubject = rdfSubject;
        this.rdfClass = rdfClass;
    }

    public Map<String, Map<String, String>> getCustom_metadata()
    {
        return custom_metadata;
    }

    public void setCustom_metadata(Map<String, Map<String, String>> custom_metadata)
    {
        this.custom_metadata = custom_metadata;
    }

    public String getRowIdentifier()
    {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier)
    {
        this.rowIdentifier = rowIdentifier;
    }

    public String getRdfSubject()
    {
        return rdfSubject;
    }

    public void setRdfSubject(String rdfSubject)
    {
        this.rdfSubject = rdfSubject;
    }

    public String getRdfClass()
    {
        return rdfClass;
    }

    public void setRdfClass(String rdfClass)
    {
        this.rdfClass = rdfClass;
    }

    public List<String>  getWarnings()
    {
        return warnings;
    }

    public void setWarnings(List<String>  warnings)
    {
        this.warnings = warnings;
    }
}
