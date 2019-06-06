package com.socrata.utils;

import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import com.socrata.utils.RowUpdateOption;

/**
 * Test the RowUpdateOption
 */
public class TestRowUpdateOption {
  private static Map<String,String[]> parameterMap;


  @BeforeClass
  public static void setupClass() {
    parameterMap = new HashMap<String,String[]>();
    parameterMap.put("truncate", new String[]{"false"});
    parameterMap.put("mergeInsteadOfReplace", new String[]{"true"});
    parameterMap.put("errorsAreFatal", new String[]{"true"});
    parameterMap.put("nonFataRowErrors[]", new String[]{"no_such_row_to_delete", "no_such_row_to_replace"});
    parameterMap.put("expectedDataVersion", new String[]{"15"});
  }

  @Test
  public void testReadParameterMap() throws IOException
  {
    RowUpdateOption options = new RowUpdateOption();
    // Test against empty parameterMap
    options.fromMap(new HashMap<String,String[]>());
    TestCase.assertNull(options.truncate);
    TestCase.assertNull(options.mergeInsteadOfReplace);
    TestCase.assertNull(options.errorsAreFatal);
    TestCase.assertNull(options.nonFatalRowErrors);
    TestCase.assertNull(options.expectedDataVersion);
    // Test they are no longer null and have the correct values
    options.fromMap(parameterMap);
    TestCase.assertFalse(options.truncate);
    TestCase.assertTrue(options.mergeInsteadOfReplace);
    TestCase.assertTrue(options.errorsAreFatal);
    TestCase.assertEquals(parameterMap.get("nonFatalRowErrors[]"), options.nonFatalRowErrors);
    TestCase.assertEquals(Long.valueOf(15), options.expectedDataVersion);
  }
}
