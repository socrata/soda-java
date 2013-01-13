package com.socrata.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * General util functions, that could have come from elsewhere, but don't want libraries
 */
public class GeneralUtils
{

    /**
     * Closes a stream, while ignoring any exceptions.
     *
     * @param is input stream to close
     */
    public static void closeQuietly(final InputStream is) {
        try {
            if (is != null) {
                is.close();
            }

        } catch (IOException ioe) {
            //Ignore
        }
    }

    /**
     * Returns the canonical path for a file, but if there is an
     * IO exception while trying to get it, will return an absolute path.
     *
     * @param file input stream to close
     */
    public static String bestFilePath(final File file) {

        try {
            return file.getCanonicalPath();
        } catch (IOException ioe) {
            return file.getAbsolutePath();
        }

    }
}
