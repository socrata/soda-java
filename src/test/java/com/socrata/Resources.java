package com.socrata;

import java.io.File;
import java.net.URL;

/**
 * Utilities for accessing test resources
 */
public class Resources {

    public static File file(final String resourcePath) {
        return new File(Resources.class.getResource(resourcePath).getFile());
    }

    public static URL url(final String resourcePath) {
        return Resources.class.getResource(resourcePath);
    }
}
