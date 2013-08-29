package com.socrata.model;

import com.socrata.model.importer.DatasetInfo;
import com.socrata.model.importer.Grant;
import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 */
public class DatasetInfoTest
{

    ObjectMapper mapper = new ObjectMapper();

    public static final File VIEW_W_PERMISSIONS = new File("src/test/resources/ViewWithPermissions.json");


    @Test
    public void testNoUserId() throws IOException
    {
        DatasetInfo datasetInfo =  mapper.readValue(VIEW_W_PERMISSIONS, DatasetInfo.class);
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
