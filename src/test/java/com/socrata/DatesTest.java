package com.socrata;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.socrata.api.Soda2Consumer;
import com.socrata.api.Soda2Producer;
import com.socrata.builders.SoqlQueryBuilder;
import com.socrata.exceptions.DoesNotExistException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.DeleteRecord;
import com.socrata.model.Meta;
import com.socrata.model.UpsertResult;
import com.socrata.model.soql.ConditionalExpression;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.utils.ColumnUtil;
import junit.framework.TestCase;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import test.model.Nomination;
import test.model.NominationWithJoda;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *   Tests the Date datatypes, and their usage through the API
 */
public class DatesTest extends TestBase
{
    @BeforeClass
    public static void useUTCTimezone()
    {
        System.setProperty("user.timezone", "UTC");
    }

    /**
     * Tests pulling out Floating Timestamps from SODA2, and surfacing them as Dates.
     */
    @Test
    public void testFloatingDateSoql() throws IOException, InterruptedException, SodaError
    {

        Date d = new Date(112, 5, 20, 7, 0);

        Soda2Consumer   consumer = createConsumer();

        //  Run an equality filter on a floating timestamp
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + "=to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();
        final List<Nomination>  results = consumer.query(NOMINATION_DATA_SET, query, Nomination.LIST_TYPE);


        // Verify results against known values.
        final Set<String>       names = Sets.newHashSet("Masumoto, David", "Trottenberg, Polly Ellen");
        for (Nomination n: results) {
            TestCase.assertTrue(names.remove(n.getName()));
            TestCase.assertEquals(d, n.getNominationDate());
        }

        //  Check greater than floating timestamp
        SoqlQuery queryGreater = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + ">to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();
        final List<Nomination>  resultsGreater = consumer.query(NOMINATION_DATA_SET, queryGreater, Nomination.LIST_TYPE);


        //  Verify results all come after the test date
        for (Nomination n: resultsGreater) {
            TestCase.assertTrue(n.getNominationDate().after(d));
        }

        //  Check greater than floating timestamp
        SoqlQuery queryLesser = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + "<to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsLesser = consumer.query(NOMINATION_DATA_SET, queryLesser, Nomination.LIST_TYPE);

        // Verify results all come before the test date
        for (Nomination n: resultsLesser) {
            TestCase.assertTrue(n.getNominationDate().before(d));
        }

        /*

        UNCOMMENT WHEN NOT EQUALS IS WORKING

        //
        //  Check not equal to floating timestamp
        SoqlQuery queryNotEqual = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + "!=to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsNotEqual = consumer.query(NOMINATION_DATA_SET, queryNotEqual, Nomination.LIST_TYPE);

        // Verify results are not the test date
        for (Nomination n: resultsNotEqual) {
            TestCase.assertFalse(n.getNominationDate().equals(d));
        }

        TestCase.assertEquals(resultsNotEqual.size(), resultsGreater.size() + resultsLesser.size());
        */
    }

    @Test
    public void testFloatingDateSoqlWJoda() throws IOException, InterruptedException, SodaError
    {
        Date d = new Date(112, 5, 20, 7, 0);

        Soda2Consumer   consumer = createConsumer();

        //
        //  Check floating timestamp
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + "=to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<NominationWithJoda>  results = consumer.query(NOMINATION_DATA_SET, query, NominationWithJoda.LIST_TYPE);
        final Set<String>       names = Sets.newHashSet("Masumoto, David", "Trottenberg, Polly Ellen");

        for (NominationWithJoda n: results) {
            TestCase.assertTrue(names.remove(n.getName()));
            TestCase.assertTrue(n.getNominationDate().toDateTime(DateTimeZone.getDefault()).toDate().equals(d));
        }

        //
        //  Check greater than floating timestamp
        SoqlQuery queryGreater = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + ">to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<NominationWithJoda>  resultsGreater = consumer.query(NOMINATION_DATA_SET, queryGreater, NominationWithJoda.LIST_TYPE);

        for (NominationWithJoda n: resultsGreater) {
            TestCase.assertTrue(n.getNominationDate().toDateTime(DateTimeZone.getDefault()).isAfter(d.getTime()));
        }

        //
        //  Check greater than floating timestamp
        SoqlQuery queryLesser = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + "<to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<NominationWithJoda>  resultsLesser = consumer.query(NOMINATION_DATA_SET, queryLesser, NominationWithJoda.LIST_TYPE);

        for (NominationWithJoda n: resultsLesser) {
            TestCase.assertTrue(n.getNominationDate().toDateTime(DateTimeZone.getDefault()).isBefore(d.getTime()));
        }

        /*

        UNCOMMENT WHEN NOT EQUALS IS WORKING

        //
        //  Check not equal to floating timestamp
        SoqlQuery queryNotEqual = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Nomination Date")  + "!=to_floating_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsNotEqual = consumer.query(NOMINATION_DATA_SET, queryNotEqual, Nomination.LIST_TYPE);

        for (Nomination n: resultsNotEqual) {
            TestCase.assertFalse(n.getNominationDate().equals(d));
        }

        TestCase.assertEquals(resultsNotEqual.size(), resultsGreater.size() + resultsLesser.size());
        */
    }

    @Test
    public void testDateCRUD() throws IOException, InterruptedException, SodaError
    {

        final Date nominationDate = DateUtils.setMilliseconds(DateUtils.addDays(new Date(), -7), 0);
        final Date voteDate = new Date();

        final Nomination  nomination = new Nomination(UUID.randomUUID().toString(), "Test Agent", "Department of Testing", "https://sandbox.demo.socrata.com", nominationDate, null, false, false);
        final Nomination  nominationUpdate = new Nomination(nomination.getName(), "Test Agent", "Department of Testing", "https://sandbox.demo.socrata.com", nominationDate, voteDate, true, false);

        final Soda2Producer producer= createProducer();

        final Meta objectMetadata = producer.addObject(UPDATE_DATA_SET, nomination);
        final Nomination createdNomination = producer.getById(UPDATE_DATA_SET, objectMetadata.getId(), Nomination.class);
        TestCase.assertEquals(nomination, createdNomination);

        final Meta updateMeta= producer.update(UPDATE_DATA_SET, objectMetadata.getId(), nominationUpdate);
        TestCase.assertEquals(objectMetadata.getId(), updateMeta.getId());

        TestCase.assertEquals(objectMetadata.getCreatedAt(), updateMeta.getCreatedAt());
        TestCase.assertEquals(objectMetadata.getCreatedMeta(), updateMeta.getCreatedMeta());
        TestCase.assertFalse(objectMetadata.getCreatedAt().after(updateMeta.getUpdatedAt()));

        final Nomination updatedNomination = producer.getById(UPDATE_DATA_SET, objectMetadata.getId(), Nomination.class);

        //UNDONE(willpugh) -- Turn back on when bug 7102 is fixed.
        //TestCase.assertTrue(EqualsBuilder.reflectionEquals(nominationUpdate, updatedNomination));

        List l = Lists.newArrayList(new DeleteRecord(objectMetadata.getId(), true));
        UpsertResult result = producer.upsert(UPDATE_DATA_SET, l);
        try {
            final Nomination deletedNomination = producer.getById(UPDATE_DATA_SET, objectMetadata.getId(), Nomination.class);
            TestCase.fail();
        } catch (DoesNotExistException e) {
            //Expected Condition
        }


        //UNDONE(willpugh) -- Uncomment when bug
        /**
        producer.delete(UPDATE_DATA_SET, objectMetadata.getId());
        final Nomination deletedNomination = producer.getById(UPDATE_DATA_SET, objectMetadata.getId(), Nomination.class);
        TestCase.assertNull(deletedNomination);
        **/
    }


    @Test
    public void testFixedDateSoql() throws IOException, InterruptedException, SodaError
    {
        Date d = new Date(112, 5, 20, 7, 0);

        Soda2Consumer   consumer = createConsumer();

        //
        //  Check floating timestamp
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + "=to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  results = consumer.query(NOMINATION_DATA_SET, query, Nomination.LIST_TYPE);
        final Set<String>       names = Sets.newHashSet("Masumoto, David", "Trottenberg, Polly Ellen");

        for (Nomination n: results) {
            TestCase.assertTrue(names.remove(n.getName()));
            TestCase.assertEquals(d, n.getNominationDate());
        }

        //
        //  Check greater than floating timestamp
        SoqlQuery queryGreater = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + ">to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsGreater = consumer.query(NOMINATION_DATA_SET, queryGreater, Nomination.LIST_TYPE);

        for (Nomination n: resultsGreater) {
            if (n.getConfirmationVoteDate() != null) {
                TestCase.assertTrue(d.before(n.getConfirmationVoteDate()));
            }
        }

        //
        //  Check greater than floating timestamp
        SoqlQuery queryLesser = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + "<to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsLesser = consumer.query(NOMINATION_DATA_SET, queryLesser, Nomination.LIST_TYPE);

        for (Nomination n: resultsLesser) {
            if (n.getConfirmationVoteDate() != null) {
                TestCase.assertTrue(d.after(n.getConfirmationVoteDate()));
            }
        }


        /*

        UNCOMMENT WHEN NOT EQUALS IS WORKING

        //
        //  Check not equal to fixed timestamp
        SoqlQuery queryNotEqual = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + "!=to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsNotEqual = consumer.query(NOMINATION_DATA_SET, queryNotEqual, Nomination.LIST_TYPE);

        for (Nomination n: resultsNotEqual) {
            if (n.getConfirmationVoteDate() != null) {
                TestCase.assertFalse(n.getConfirmationVoteDate().equals(d));
            }
        }

        TestCase.assertEquals(resultsNotEqual.size(), resultsGreater.size() + resultsLesser.size());
        */

    }

    @Test
    public void testFixedDateSoqlJoda() throws IOException, InterruptedException, SodaError
    {
        Date d = new Date(112, 5, 20, 7, 0);

        Soda2Consumer   consumer = createConsumer();

        //
        //  Check floating timestamp
        SoqlQuery query = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + "=to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<NominationWithJoda>  results = consumer.query(NOMINATION_DATA_SET, query, NominationWithJoda.LIST_TYPE);
        final Set<String>       names = Sets.newHashSet("Masumoto, David", "Trottenberg, Polly Ellen");

        for (NominationWithJoda n: results) {
            TestCase.assertTrue(names.remove(n.getName()));
            TestCase.assertEquals(d, n.getNominationDate().toDate());
        }

        //
        //  Check greater than floating timestamp
        SoqlQuery queryGreater = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + ">to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<NominationWithJoda>  resultsGreater = consumer.query(NOMINATION_DATA_SET, queryGreater, NominationWithJoda.LIST_TYPE);

        for (NominationWithJoda n: resultsGreater) {
            if (n.getConfirmationVoteDate() != null) {
                TestCase.assertTrue(d.before(n.getConfirmationVoteDate().toDate()));
            }
        }

        //
        //  Check greater than floating timestamp
        SoqlQuery queryLesser = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + "<to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<NominationWithJoda>  resultsLesser = consumer.query(NOMINATION_DATA_SET, queryLesser, NominationWithJoda.LIST_TYPE);

        for (NominationWithJoda n: resultsLesser) {
            if (n.getConfirmationVoteDate() != null) {
                TestCase.assertTrue(d.after(n.getConfirmationVoteDate().toDate()));
            }
        }


        /*

        UNCOMMENT WHEN NOT EQUALS IS WORKING

        //
        //  Check not equal to fixed timestamp
        SoqlQuery queryNotEqual = new SoqlQueryBuilder()
                .setWhereClause(new ConditionalExpression(ColumnUtil.getQueryName("Confirmation Vote")  + "!=to_fixed_timestamp('2012-6-20T07:00:00Z')"))
                .build();

        final List<Nomination>  resultsNotEqual = consumer.query(NOMINATION_DATA_SET, queryNotEqual, Nomination.LIST_TYPE);

        for (Nomination n: resultsNotEqual) {
            if (n.getConfirmationVoteDate() != null) {
                TestCase.assertFalse(n.getConfirmationVoteDate().equals(d));
            }
        }

        TestCase.assertEquals(resultsNotEqual.size(), resultsGreater.size() + resultsLesser.size());
        */

    }


}
