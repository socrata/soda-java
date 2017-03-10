package com.socrata.builders;

import com.google.common.collect.Lists;
import com.socrata.model.soql.ConditionalExpression;
import com.socrata.model.soql.OrderByClause;
import com.socrata.model.soql.SoqlQuery;
import com.socrata.model.soql.SortOrder;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the SoqlQueryBuilder
 */
public class SoqlQueryBuilderTest extends TestCase
{

    public static final SoqlQuery SIMPLE_WHERE = new SoqlQueryBuilder()
        .setWhereClause(new ConditionalExpression("1==1"))
        .build();

    public static final SoqlQuery ROBUST_QUERY = new SoqlQueryBuilder()
        .addSelectPhrase("Column1")
        .setWhereClause(new ConditionalExpression("1==1"))
        .addGroupByPhrase("Group1")
        .setHavingClause(new ConditionalExpression("2==2"))
        .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, "Order1"))
        .setLimit(10)
        .setOffset(2)
        .build();

    public static final SoqlQuery FULL_TEXT_SEARCH = new SoqlQueryBuilder()
        .addSelectPhrase("Column1")
        .setFullTextSearchClause("CLINTON")
        .addOrderByPhrase(new OrderByClause(SortOrder.Ascending, "Order1"))
        .setLimit(100)
        .setOffset(0)
        .build();


    /**
     * Simply makes sure the easiest builder case works.
     */
    @Test
    public  void testSimpleBuilder() {

        validateSimpleQuery(SIMPLE_WHERE);
    }

    /**
     * Simply makes sure the Robust Query case works.
     */
    @Test
    public void testRobustQuery() {
        validateRobustQuery(ROBUST_QUERY);
    }

    /**
     * Simply makes sure the Robust Query case works.
     */
    @Test
    public void testFullTextQuery() {
        validateFullTextQuery(FULL_TEXT_SEARCH);
    }

    /**
     * Tests the constructor for SoqlQueryBuilder that takes a SoqlQuery as a parameter.
     */
    @Test
    public void testQueryConstructor() {
        validateSimpleQuery(new SoqlQueryBuilder(SIMPLE_WHERE).build());
        validateRobustQuery(new SoqlQueryBuilder(ROBUST_QUERY).build());
        validateFullTextQuery(new SoqlQueryBuilder(FULL_TEXT_SEARCH).build());
    }

    public void testAddAllMethods() {
        SoqlQueryBuilder    builder = new SoqlQueryBuilder(ROBUST_QUERY);

        builder.addGroupByPhrases(Lists.newArrayList("Group2", "Group3"));
        builder.addSelectPhrases(Lists.newArrayList("Column2", "Column3"));
        builder.addOrderByPhrases(Lists.newArrayList(new OrderByClause(SortOrder.Descending, "Order2"),
                                                     new OrderByClause(SortOrder.Ascending, "Order3")));

        SoqlQuery   query = builder.build();
        assertEquals(3, query.groupByClause.size());
        assertEquals(3, query.selectClause.size());
        assertEquals(3, query.orderByClause.size());


        assertThat(query.groupByClause, equalTo(Arrays.asList("Group1", "Group2", "Group3")));

        assertThat(query.selectClause, equalTo(Arrays.asList("Column1", "Column2", "Column3")));

        assertThat(query.orderByClause, equalTo(Arrays.asList(new OrderByClause(SortOrder.Ascending, "Order1"),
                new OrderByClause(SortOrder.Descending, "Order2"),
                new OrderByClause(SortOrder.Ascending, "Order3"))));
    }

    public void testSetMethodsOnCollections() {
        SoqlQueryBuilder    builder = new SoqlQueryBuilder(ROBUST_QUERY);

        builder.setGroupByPhrase(Lists.newArrayList("Group2", "Group3"));
        builder.setSelectPhrase(Lists.newArrayList("Column2", "Column3"));
        builder.setOrderByPhrase(Lists.newArrayList(new OrderByClause(SortOrder.Descending, "Order2"),
                                                     new OrderByClause(SortOrder.Ascending, "Order3")));

        SoqlQuery   query = builder.build();
        assertEquals(2, query.groupByClause.size());
        assertEquals(2, query.selectClause.size());
        assertEquals(2, query.orderByClause.size());

        assertThat(query.groupByClause, equalTo(Arrays.asList("Group2", "Group3")));

        assertThat(query.selectClause, equalTo(Arrays.asList("Column2", "Column3")));

        assertThat(query.orderByClause, equalTo(Arrays.asList(
                new OrderByClause(SortOrder.Descending, "Order2"),
                new OrderByClause(SortOrder.Ascending, "Order3"))));
    }



    private void validateSimpleQuery(final SoqlQuery query) {
        assertNull(query.selectClause);
        assertTrue("1==1".equals(query.whereClause.toString()));
        assertNull(query.groupByClause);
        assertNull(query.havingClause);
        assertNull(query.fullTextSearchClause);
        assertNull(query.orderByClause);
        assertNull(query.limit);
        assertNull(query.offset);
    }

    private void validateRobustQuery(final SoqlQuery query) {

        assertEquals(1, query.selectClause.size());
        assertEquals("Column1", query.selectClause.get(0));
        assertTrue("1==1".equals(query.whereClause.toString()));
        assertEquals(1, query.groupByClause.size());
        assertEquals("Group1", query.groupByClause.get(0));
        assertTrue("2==2".equals(query.havingClause.toString()));
        assertNull(query.fullTextSearchClause);
        assertEquals(1, query.orderByClause.size());
        assertEquals(SortOrder.Ascending, query.orderByClause.get(0).sortOrder);
        assertEquals("Order1", query.orderByClause.get(0).columnName);
        assertEquals(Integer.valueOf(10), query.limit);
        assertEquals(Integer.valueOf(2), query.offset);

    }

    private void validateFullTextQuery(final SoqlQuery query) {

        assertEquals(1, query.selectClause.size());
        assertEquals("Column1", query.selectClause.get(0));
        assertEquals("CLINTON", query.fullTextSearchClause);
        assertNull(query.groupByClause);
        assertNull(query.havingClause);
        assertNull(query.whereClause);
        assertEquals(1, query.orderByClause.size());
        assertEquals(SortOrder.Ascending, query.orderByClause.get(0).sortOrder);
        assertEquals("Order1", query.orderByClause.get(0).columnName);
        assertEquals(Integer.valueOf(100), query.limit);
        assertEquals(Integer.valueOf(0), query.offset);
    }


}
