package com.socrata.utils;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    /**
     * Reads in a CSV file as maps of objects.  You cannot assume anything
     * smart happens in terms of type conversion, so everything will be a String.
     *
     * @param csvFile file to read in
     * @return List of objects hydrated from the csv file.  Each object will be a map from column
     * name to value.
     * @throws IOException
     */
    public static List<Map<String, Object>> readInCsv(final File csvFile) throws IOException
    {
        FileReader  fileReader = new FileReader(csvFile);
        CSVReader   reader = new CSVReader(fileReader);

        List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>();
        String[]    headers = reader.readNext();
        if (headers != null) {
            String[]    currLine;
            while ((currLine = reader.readNext()) != null) {
                ImmutableMap.Builder<String, Object>    builder = ImmutableMap.builder();
                for (int i=0; i<headers.length; i++) {
                    if (StringUtils.isNotEmpty(currLine[i])) {
                        builder.put(headers[i], currLine[i]);
                    }
                }

                retVal.add(builder.build());
            }
        }

        return retVal;
    }
}
