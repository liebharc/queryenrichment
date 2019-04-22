package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;

public class FromFilterEnrichment<T> extends Enrichment<T> {

    private final SimpleExpression expression;

    public FromFilterEnrichment(Attribute<T> attribute, SimpleExpression expression) {
        super(attribute, NO_COLUMN);
        this.expression = expression;
    }

    @Override
    public void enrich(IntermediateResult result) {
        result.add(this, expression.getValue());
    }

    @Override
    public List<Attribute<?>> getDependencies() {
        return Collections.emptyList();
    }
}
