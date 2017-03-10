package com.socrata.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socrata.Resources;
import com.socrata.model.importer.DatasetInfo;
import com.socrata.utils.ObjectMapperFactory;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

/**
 */
public class DatasetInfoTest
{

    private static final ObjectMapper mapper = ObjectMapperFactory.create();

    @Test
    public void testNoUserId() throws IOException
    {
        DatasetInfo datasetInfo =  mapper.readValue(Resources.url("/ViewWithPermissions.json"), DatasetInfo.class);
        TestCase.assertNotNull(datasetInfo);
        TestCase.assertNotNull(datasetInfo.getGrants());
        TestCase.assertNotNull(datasetInfo.getTableAuthor());
        TestCase.assertEquals("8910-1234", datasetInfo.getTableAuthor().getId());
        TestCase.assertEquals("Table Author", datasetInfo.getTableAuthor().getDisplayName());
        TestCase.assertEquals("Table Author Screen Name", datasetInfo.getTableAuthor().getScreenName());


        TestCase.assertNotNull(datasetInfo.getOwner());
        TestCase.assertEquals("1234-5678", datasetInfo.getOwner().getId());
        TestCase.assertEquals("Test Owner", datasetInfo.getOwner().getDisplayName());
        TestCase.assertEquals("Test Owner Screen Name", datasetInfo.getOwner().getScreenName());



    }

}
