package com.github.liebharc.queryenricher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Selector {

    private final Attribute attributes;
    private final String column;

    public Selector(Attribute attribute, String columnOrNull) {
        this.attributes = attribute;
        this.column = columnOrNull;
    }

    public Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    public Attribute getAttribute() {
        return attributes;
    }

    public void enrich(IntermediateResult result) {
        result.addFromQuery(this);
    }

    public List<Class<? extends Selector>> getDependencies() {
        return Collections.emptyList();
    }
}
