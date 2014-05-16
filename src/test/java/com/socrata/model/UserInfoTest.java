package com.socrata.model;

import com.socrata.model.importer.UserInfo;
import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: willpugh
 * Date: 8/28/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserInfoTest
{

    ObjectMapper mapper = new ObjectMapper();

    public static final String  JSON_TEST = "{\n" +
            "    \"id\" : \"et53-5za7\",\n" +
            "    \"displayName\" : \"Will Pugh\",\n" +
            "    \"emailUnsubscribed\" : false,\n" +
            "    \"privacyControl\" : \"login\",\n" +
            "    \"profileLastModified\" : 1373237944,\n" +
            "    \"screenName\" : \"Will Pugh\",\n" +
            "    \"rights\" : [ \"create_datasets\", \"edit_others_datasets\", \"edit_sdp\", \"edit_site_theme\", \"moderate_comments\", \"manage_users\", \"chown_datasets\", \"edit_nominations\", \"approve_nominations\", \"feature_items\", \"federations\", \"manage_stories\", \"manage_approval\", \"change_configurations\", \"view_domain\", \"view_others_datasets\", \"edit_pages\", \"create_pages\" ],\n" +
            "    \"flags\" : [ \"admin\" ]\n" +
            "  }";

    public static final String  JSON_TEST_NO_ITEMS = "{\n" +
            "    \"rights\" : [ \"create_datasets\", \"edit_others_datasets\", \"edit_sdp\", \"edit_site_theme\", \"moderate_comments\", \"manage_users\", \"chown_datasets\", \"edit_nominations\", \"approve_nominations\", \"feature_items\", \"federations\", \"manage_stories\", \"manage_approval\", \"change_configurations\", \"view_domain\", \"view_others_datasets\", \"edit_pages\", \"create_pages\" ],\n" +
            "    \"flags\" : [ \"admin\" ]\n" +
            "  }";

    @Test
    public void testSerialization() throws Exception
    {
        UserInfo userInfo =  mapper.readValue(JSON_TEST, UserInfo.class);
        TestCase.assertNotNull(userInfo);
        TestCase.assertEquals(userInfo.getId(), "et53-5za7");
        TestCase.assertEquals(userInfo.getDisplayName(), "Will Pugh");
        TestCase.assertEquals(userInfo.getScreenName(), "Will Pugh");

        UserInfo userInfo2 =  mapper.readValue(JSON_TEST_NO_ITEMS, UserInfo.class);
        TestCase.assertNotNull(userInfo2);
        TestCase.assertNull(userInfo2.getId());
        TestCase.assertNull(userInfo2.getDisplayName());
        TestCase.assertNull(userInfo2.getScreenName());
    }
}
