package com.socrata.model.soql;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An expression that combines multiple expressions with either an "AND" or an "OR"
 * operation.
 */
public class CompositeExpression implements Expression {

    public final CompositeOperations ops;
    public final ImmutableList<Expression> children;

    /**
     * Constructor.
     *
     * @param ops operation for combining the logic for the children.  Could be AND or OR
     * @param children children operations.
     */
    public CompositeExpression(@Nonnull final CompositeOperations ops, @Nonnull final List<Expression> children) {
        this.ops = ops;
        this.children = (children instanceof ImmutableList) ? (ImmutableList) children : ImmutableList.copyOf(children);
    }


    /**
     * Constructor that creates an expression that combines child expressions with AND operations.
     *
     * @param children children operations.
     */
    public CompositeExpression(List<Expression> children) {
        this(CompositeOperations.AND, children);
    }

    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return StringUtils.join(children, " " + ops.name() + " ");
    }
}
