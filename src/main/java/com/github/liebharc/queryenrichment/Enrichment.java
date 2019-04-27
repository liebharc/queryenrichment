package com.github.liebharc.queryenrichment;

import java.util.Optional;

/**
 * An enrichment produces results by combining values from other attributes and/or other data sources.
 * @param <T> Attribute type
 */
public abstract class Enrichment<T> implements Step<T> {

    private static final long serialVersionUID = -387954492411088733L;

    /** The attribute which is set by this step */
    private final Attribute<T> attribute;
    /** Optional: Query column, most sof the time this value should be null as most enrichment don't directly query */
    private final String column;
    /** Dependencies of this step */
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
