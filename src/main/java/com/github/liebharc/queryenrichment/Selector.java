package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Selector {

    public static final String NO_COLUMN = null;

    private final Attribute<?> attributes;
    private final String column;

    public Selector(Attribute<?> attribute, String columnOrNull) {
        this.attributes = attribute;
        this.column = columnOrNull;
    }

    public Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    public Attribute<?> getAttribute() {
        return attributes;
    }

    public void enrich(IntermediateResult result) {
        result.addFromQuery(this);
    }

    public List<Attribute<?>> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Selector{" +
                "attributes=" + attributes +
                '}';
    }
}
