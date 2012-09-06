package com.socrata.model.soql;

/**
 * The conditional expressions to actually get evaluated against rows.
 */
public class ConditionalExpression implements Expression {

    final String expression;

    public ConditionalExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return expression;
    }
}
