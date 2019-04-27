package com.github.liebharc.queryenrichment;

/**
 * Consider a SQL statement like this:
 * <pre>SELECT * FROM STUDENT WHERE CLASS = 123;</pre>
 * If we then ask what class a student belongs to we can say from the WHERE filter condition that the class ID must be
 * 123. This class allows to reflect that knowledge.
 * @param <T> Attribute type
 */
public class AddValuesFromFilter<T> extends Enrichment<T> {

    private static final long serialVersionUID = 2553654683345913539L;

    /** An equality expression */
    private final SimpleExpression expression;

    public AddValuesFromFilter(Attribute<T> attribute, SimpleExpression expression) {
        super(attribute, NO_COLUMN, Dependencies.noDependencies());
        this.expression = expression;
    }

    @Override
    public void enrich(IntermediateResult result) {
        result.add(this, (T)expression.getValue());
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
