package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Selector<T> implements Step<T> {

    private final Attribute<T> attribute;
    private final String column;

    public Selector(Attribute<T> attribute, String columnOrNull) {
        this.attribute = attribute;
        this.column = columnOrNull;
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
    public List<Attribute<?>> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Step{" +
                "attribute=" + attribute +
                '}';
    }
}
