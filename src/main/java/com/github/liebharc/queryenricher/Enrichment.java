package com.github.liebharc.queryenricher;

import java.util.List;
import java.util.Optional;

public abstract class Enrichment extends Selector {

    public Enrichment(Attribute attribute, String columnOrNull) {
        super(attribute, columnOrNull);
    }

    @Override
    public abstract void enrich(IntermediateResult result);

    @Override
    public abstract List<Class<? extends Selector>> getDependencies();

    @Override
    public final Optional<String> getColumn() {
        return Optional.empty();
    }
}
