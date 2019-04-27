package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;

public class AddValuesFromFilter<T> extends Enrichment<T> {

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
