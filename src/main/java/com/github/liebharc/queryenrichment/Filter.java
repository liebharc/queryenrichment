package com.github.liebharc.queryenrichment;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

class Filter<T> implements Step<T> {

    private final Step<T> innerStep;
    private final SimpleExpression expression;

    Filter(Step<T> innerStep, SimpleExpression expression) {
        this.innerStep = innerStep;
        this.expression = expression;
    }

    @Override
    public Optional<String> getColumn() {
        return innerStep.getColumn();
    }

    @Override
    public Attribute<T> getAttribute() {
        return innerStep.getAttribute();
    }

    @Override
    public void enrich(IntermediateResult result) {
        innerStep.enrich(result);
        final T value = result.get(this.getAttribute());
        if (!expression.getOperation().equals("=")) {
            throw new IllegalArgumentException("Only equality is supported right now");
        }

        if (!Objects.equals(value, expression.getValue())) {
            result.stopProcessing();
        }
    }

    @Override
    public List<Attribute<?>> getDependencies() {
        return innerStep.getDependencies();
    }

    @Override
    public boolean isConstant() {
        return innerStep.isConstant();
    }
}
