package com.github.liebharc.queryenrichment;

import java.util.Optional;

public abstract class Enrichment<T> implements Step<T> {

    private final Attribute<T> attribute;
    private final String column;
    private final Dependency dependency;

    public Enrichment(Attribute<T> attribute, Dependency dependency) {
        this(attribute, null, dependency);
    }

    public Enrichment(Attribute<T> attribute, String columnOrNull, Dependency dependency) {
        this.attribute = attribute;
        this.column = columnOrNull;
        this.dependency = dependency;
    }

    @Override
    public abstract void enrich(IntermediateResult result);

    @Override
    public final Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    @Override
    public Dependency getDependencies() {
        return dependency;
    }

    @Override
    public Attribute<T> getAttribute() {
        return attribute;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public String toString() {
        return "Enrichment{" +
                "attribute=" + attribute +
                ", column='" + column + '\'' +
                ", dependency=" + dependency +
                '}';
    }
}
