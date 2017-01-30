package com.socrata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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
    public SodaErrorResponse(@JsonProperty("errorCode") String code, @JsonProperty("message") String message, @JsonProperty("error") String error, @JsonProperty("data") JsonNode data)
    {
        this.code = code;
        this.message = message;
        this.error = error;
        this.data = data;
    }
}
