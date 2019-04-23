package com.github.liebharc.queryenrichment;

import java.util.List;
import java.util.Optional;

public abstract class Enrichment<T> implements Step<T> {

    private final Attribute<T> attribute;
    private final String column;

    public Enrichment(Attribute<T> attribute) {
        this(attribute, null);
    }

    public Enrichment(Attribute<T> attribute, String columnOrNull) {
        this.attribute = attribute;
        this.column = columnOrNull;
    }

    @Override
    public abstract void enrich(IntermediateResult result);

    @Override
    public abstract List<Attribute<?>> getDependencies();

    @Override
    public final Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    @Override
    public Attribute<T> getAttribute() {
        return attribute;
    }

    @Override
    public boolean isConstant() {
        return false;
    }
}
