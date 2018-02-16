package com.socrata.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule;

import java.text.*;
import java.util.Date;
import java.util.TimeZone;

/**
 * Produces an instances of {@link ObjectMapper} for use with the Socrata API
 *
 * This is required to get "slightly" custom date parsing.  The default Jackson behaviour will
 * turn a floating timestamp <code>2012-06-20T07:00:00</code> into Zulu time <code>2012-06-20T07:00:00<b>Z</b></code>
 *
 * This leads to problems if transforming into a Date or DateTime (although Joda's LocalDateTime would work fine)
 */
public final class ObjectMapperFactory {

    protected static final SimpleDateFormat SOCRATA_WRITING_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    protected static final SimpleDateFormat SOCRATA_FLOATING_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    protected static final SimpleDateFormat SOCRATA_FLOATING_FORMAT_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    protected static final DateFormat[] SPECIAL_DATE_LIST = new DateFormat[] {SOCRATA_FLOATING_FORMAT_MILLIS, SOCRATA_FLOATING_FORMAT} ;

    public static ObjectMapper create() {
        return new ObjectMapper()
                .registerModule(new JodaModule())
                .registerModule(new Jaxrs2TypesModule())
                .setDateFormat(new ObjectMapperFactory.SocrataDateFormat());
    }

    /**
     * A class that special cases ISO 8601 dates, and assumes that no "Z" at
     * the end means that it should be translated as local time instead of Zulu time.
     *
     * This makes the Socrata floating date types work.
     */
    protected static class SocrataDateFormat extends StdDateFormat
    {

        //Although, SimpleDateFormat is not thread safe, the entire SocrataDateFormat should be getting cloned by Jackson.
        final SimpleDateFormat SOCRATA_WRITE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final SimpleDateFormat SOCRATA_FLOATING_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final SimpleDateFormat SOCRATA_FLOATING_FORMAT_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        final DateFormat[] SPECIAL_DATE_LIST = new DateFormat[] {SOCRATA_FLOATING_FORMAT_MILLIS, SOCRATA_FLOATING_FORMAT} ;


        public SocrataDateFormat()
        {
            final TimeZone localTimezone = TimeZone.getDefault();
            SOCRATA_WRITE_FORMAT.setTimeZone(localTimezone);
        }

        /**
         * Overrides this method to special case ISO 8601 dates without 'Z'
         * to be local date/time.
         *
         * @param dateStr the string to parse
         * @param pos current position in the string
         * @return the Date returned.
         */
        @Override
        protected Date parseAsISO8601(String dateStr, ParsePosition pos, boolean throwErrors) throws ParseException {
            final Date retVal = parseAsFloatingISO8601(dateStr, pos);
            if (retVal != null) {
                return retVal;
            }
            return super.parseAsISO8601(dateStr, pos, throwErrors);
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
        {
            return SOCRATA_WRITING_FORMAT.format(date, toAppendTo, fieldPosition);
        }

        /**
         * Parses an ISO8601 that does not contain a Z as a floating type.
         *
         * @param dateString the date string to parse
         * @param pos the position to start parsin gat.
         * @return
         */
        private Date parseAsFloatingISO8601(final String dateString, final ParsePosition pos)
        {
            final int len = dateString.length();
            final char c = dateString.charAt(len-1);
            if (c != 'z' && c != 'Z') {

                for (DateFormat format : SPECIAL_DATE_LIST)
                {
                    final ParsePosition testPos = new ParsePosition(pos.getIndex());
                    final DateFormat safeFormat = (DateFormat) format.clone();
                    final Date retVal = safeFormat.parse(dateString, testPos);

                    if (retVal != null && testPos.getIndex()==len) {
                        pos.setIndex(testPos.getIndex());
                        return retVal;
                    }
                }
            }
            return null;
        }

        /**
         * Need to override this, because StdDateFormat overrode this.
         *
         * @return A new SocrataDateFormat
         */
        @Override
        public StdDateFormat clone()
        {
            return new SocrataDateFormat();
        }
    }

    /**
     * static members only
     */
    private ObjectMapperFactory() { }
}
