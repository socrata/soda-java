package com.socrata.model.soql;

/**
 * Enumerates all the SoQL clauses.  Each enum contains the
 * string used to express this clause in either the SODA2 URL
 * or directly in a SoQL query.
 */
public enum SoqlClauses {

    select  ("$select", "select"),
    orderBy ("$order",  "order"),
    where   ("$where",  "where"),
    having  ("$having", "having"),
    groupBy ("$group",  "group by"),
    fullText("$q",      "search"),
    offset  ("$offset", "offset"),
    limit   ("$limit",  "limit");


    final public String urlParam;
    final public String soqlKeyword;

    private SoqlClauses(String urlParam, String soqlKeyword) {
        this.urlParam = urlParam;
        this.soqlKeyword = soqlKeyword;
    }
}
