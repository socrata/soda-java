package com.socrata.model;

import org.codehaus.jackson.JsonNode;
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
    public final String error;
    public final JsonNode data;


    @JsonCreator
    public SodaErrorResponse(@JsonProperty("code") String code, @JsonProperty("message") String message, @JsonProperty("error") String error, @JsonProperty("data") JsonNode data)
    {
        this.code = code;
        this.message = message;
        this.error = error;
        this.data = data;
    }
}
