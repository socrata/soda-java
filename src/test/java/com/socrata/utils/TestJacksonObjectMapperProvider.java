package com.socrata.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socrata.model.importer.Dataset;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test the JacksonObjectMapperProvider
 */
public class TestJacksonObjectMapperProvider {

    private static final Date base = new Date(112, 5, 20, 7, 0);

    private static ObjectMapper mapper;
    private static ObjectMapperFactory.SocrataDateFormat format;

    @BeforeClass
    public static void setupClass() {
        // Make sure the ObjectMapper is setup correctly
        final JacksonObjectMapperProvider provider = new JacksonObjectMapperProvider();
        mapper = provider.getContext(null);
        assertThat(mapper, notNullValue());
        format = (ObjectMapperFactory.SocrataDateFormat) mapper.getDeserializationConfig().getDateFormat();

        // Make sure we're running in the right timezone
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
    }

    @Test(expected = ParseException.class)
    public void testParseBadDateString() throws ParseException {
        parse("12345");
    }

    @Test
    public void testParseISONoSecondsNoTZ() throws ParseException {
        assertThat(parse("2012-6-20T07:00:00"), equalTo(base));
    }

    @Test
    public void testParseISONoTZ() throws ParseException {
        assertThat(parse("2012-6-20T07:00:00.000"), equalTo(base));
    }

    @Test
    public void testParseISONoSeconds() throws ParseException {
        assertThat(parse("2012-6-20T07:00:00-0300"), is(before(base)));
    }

    @Test
    public void testParseISO() throws ParseException {
        assertThat(parse("2012-6-20T07:00:00.000-0300"), is(before(base)));
    }

    @Test
    public void testParseISONoSecondsZulu() throws ParseException {
        assertThat(parse("2012-6-20T07:00:00Z"), is(before(base)));
    }

    @Test
    public void testParseISOZulu() throws ParseException {
        assertThat(parse("2012-6-20T07:00:00.000Z"), is(before(base)));
    }

    @Test
    public void testViewMapping() throws IOException {
        final Dataset v = mapper.readValue(TestJacksonObjectMapperProvider.class.getResource("/view.json"), Dataset.class);
        assertThat(v, notNullValue());
    }

    private static Date parse(final String str) throws ParseException {
        return format.parseAsISO8601(str, new ParsePosition(0), false);
    }

    private static BeforeDateMatcher before(final Date base) {
        return new BeforeDateMatcher(base);
    }

    private static class BeforeDateMatcher extends BaseMatcher<Date> {

        private final Date base;

        private BeforeDateMatcher(final Date base) {
            this.base = base;
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("expected date to be after " + base);
        }

        @Override
        public boolean matches(final Object o) {
            if (!(o instanceof Date)) {
                return false;
            }
            final Date subject = (Date) o;
            return subject.before(base);
        }
    }

}
