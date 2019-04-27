package com.github.liebharc.queryenrichment;

import java.util.Optional;

public class Selector<T> implements Step<T> {

    private final Attribute<T> attribute;
    private final String column;
    private final Dependency dependency;

    public Selector(Attribute<T> attribute, String columnOrNull) {
       this(attribute, columnOrNull, Dependencies.noDependencies());
    }

    public Selector(Attribute<T> attribute, String columnOrNull, Dependency dependency) {
        this.attribute = attribute;
        this.column = columnOrNull;
        this.dependency = dependency;
    }

    @Override
    public Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    @Override
    public Attribute<T> getAttribute() {
        return attribute;
    }

    @Override
    public void enrich(IntermediateResult result) {
        result.addFromQuery(this);
    }

    @Override
    public Dependency getDependencies() {
        return dependency;
    }

    @Override
    public final boolean isConstant() {
        return false;
    }

    @Override
    public String toString() {
        return "Step{" +
                "attribute=" + attribute +
                '}';
    }
}
