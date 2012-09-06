package com.socrata.utils;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a class that contains various utilities for dealing with column names a queries.
 *
 */
public class ColumnUtil
{

    /**
     * Converts a column's name into the SoQL name.  This will lowercase the names and
     * replace whitespace with a '_'
     *
     * @param name name to convert.
     * @return the converted name.  This name should be valid in SoQL.
     */
    public static String getQueryName(@Nonnull final String name) {
        return name.replace(' ', '_').toLowerCase();
    }

}
