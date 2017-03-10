package com.socrata.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socrata.TestBase;
import com.socrata.model.importer.Grant;
import com.socrata.utils.ObjectMapperFactory;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

/**
  */
public class GrantTest extends TestBase
{

    private static final ObjectMapper mapper = ObjectMapperFactory.create();

    public static final String NO_USER_ID = "{\n" +
            "    \"inherited\" : false,\n" +
            "    \"type\" : \"viewer\",\n" +
            "    \"userEmail\" : \"willpugh@gmail.com\"\n" +
            "  }";

    public static final String NO_USER_EMAIL = "{\n" +
            "    \"inherited\" : false,\n" +
            "    \"type\" : \"viewer\",\n" +
            "    \"userId\" : \"rj6s-jsfr\"\n" +
            "  }";


    @Test
    public void testNoUserId() throws IOException
    {
        Grant grant =  mapper.readValue(NO_USER_ID, Grant.class);
        TestCase.assertNotNull(grant);
        TestCase.assertNull(grant.getUserId());

        TestCase.assertEquals("willpugh@gmail.com", grant.getUserEmail());
        TestCase.assertEquals("viewer", grant.getType());
    }

    @Test
    public void testNoUserEmail() throws IOException
    {
        Grant grant =  mapper.readValue(NO_USER_EMAIL, Grant.class);
        TestCase.assertNotNull(grant);
        TestCase.assertNull(grant.getUserEmail());

        TestCase.assertEquals("rj6s-jsfr", grant.getUserId());
        TestCase.assertEquals("viewer", grant.getType());
    }
}
