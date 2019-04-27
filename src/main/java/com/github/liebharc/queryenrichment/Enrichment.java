package com.github.liebharc.queryenrichment;

import java.util.List;
import java.util.Optional;

public abstract class Enrichment<T> implements Step<T> {

    private final Attribute<T> attribute;
    private final String column;
    private final Dependency dependency;

    public Enrichment(Attribute<T> attribute) {
        this(attribute, null);
    }

    public Enrichment(Attribute<T> attribute, String columnOrNull) {
        this.attribute = attribute;
        this.column = columnOrNull;
        this.dependency = this.getDependencies();
    }

    @Override
    public abstract void enrich(IntermediateResult result);

    @Override
    public abstract Dependency getDependencies();

    @Override
    public final Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    @Override
    public Dependency getDependenciesCached() {
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
