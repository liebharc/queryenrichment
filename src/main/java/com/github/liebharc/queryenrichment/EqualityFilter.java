package com.github.liebharc.queryenrichment;

import java.util.Objects;

class EqualityFilter<T> extends Filter<T> {
    EqualityFilter(Step<T> innerStep, SimpleExpression expression) {
        super(innerStep, expression);
    }

    @Override
    public void enrich(IntermediateResult result) {
        innerStep.enrich(result);
        final T value = result.get(this.getAttribute());

        if (!Objects.equals(value, expression.getValue())) {
            result.stopProcessing();
        }
    }

    @Override
    public String toString() {
        return "EqualityFilter{" +
                "innerStep=" + innerStep +
                ", expression=" + expression +
                '}';
    }
}
