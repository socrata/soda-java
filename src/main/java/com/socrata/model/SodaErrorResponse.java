package com.socrata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SodaErrorResponse
{
    public final String code;
    public final String message;
    public final String data;


    @JsonCreator
    public SodaErrorResponse(@JsonProperty("code") String code, @JsonProperty("message") String message, @JsonProperty("data") String data)
    {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
