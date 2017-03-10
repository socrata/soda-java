package com.socrata.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Class to provide a customer ObjectMapper to Jersey (for Jackson).
 */
@Immutable
@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper>
{
    private final ObjectMapper mapper;

    public JacksonObjectMapperProvider() {
        this(ObjectMapperFactory.create());
    }

    public JacksonObjectMapperProvider(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type)
    {
        return mapper;
    }
}
