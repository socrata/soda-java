package com.socrata;

import com.google.common.collect.Lists;
import com.socrata.utils.ColumnUtil;
import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;

/**
 * Tests the utility functions in ColumnUtils.
 */
public class TestColumnUtils
{

    /**
     * Tests converting names from names with spaces, etc.  into column names.
     */
    @Test
    public void testGetQueryName() {

        List<Pair<String, String>>  testNames = Lists.newArrayList(
                Pair.of("hello", "hello"),
                Pair.of("HelLo", "hello"),
                Pair.of("Hello Kitty", "hello_kitty"),
                Pair.of(" Hello  Kitty ", "_hello__kitty_"),
                Pair.of("", "")
        );

        for (Pair<String, String> testCase : testNames) {
            TestCase.assertEquals(testCase.getRight(), ColumnUtil.getQueryName(testCase.getLeft()));
        }

    }

}
