package com.github.liebharc.queryenrichment;

import java.util.List;
import java.util.Optional;

public abstract class Enrichment<T> extends Selector<T> {

    public Enrichment(Attribute<T> attribute) {
        this(attribute, null);
    }

    public Enrichment(Attribute<T> attribute, String columnOrNull) {
        super(attribute, columnOrNull);
    }

    @Override
    public abstract void enrich(IntermediateResult result);

    @Override
    public abstract List<Attribute<?>> getDependencies();

    @Override
    public final Optional<String> getColumn() {
        return Optional.empty();
    }
}
