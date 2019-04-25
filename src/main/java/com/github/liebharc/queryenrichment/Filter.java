package com.github.liebharc.queryenrichment;

import java.util.List;
import java.util.Optional;

abstract class Filter<T> implements Step<T> {

    protected final Step<T> innerStep;
    protected final SimpleExpression expression;

     static<T> Filter<T> createFilter(Step<T> innerStep, SimpleExpression expression) {
        if (!expression.getOperation().equals("=")) {
            throw new IllegalArgumentException("Only equality is supported right now");
        }

        return new EqualityFilter<>(innerStep, expression);
    }

    protected Filter(Step<T> innerStep, SimpleExpression expression) {
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
    public abstract void enrich(IntermediateResult result);

    @Override
    public Dependency getDependencies() {
        return innerStep.getDependencies();
    }

    @Override
    public Dependency getDependenciesCached() {
        return innerStep.getDependenciesCached();
    }

    @Override
    public boolean isConstant() {
        return innerStep.isConstant();
    }

    @Override
    public String toString() {
        return "Filter{" +
                "innerStep=" + innerStep +
                ", expression=" + expression +
                '}';
    }
}
