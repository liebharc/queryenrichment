package com.github.liebharc.queryenricher;

import java.util.Optional;

public class Selector {

    private final Attribute attributes;
    private final String column;

    public Selector(Attribute attribute, String columnOrNull) {
        this.attributes = attribute;
        this.column = columnOrNull;
    }

    Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    Attribute getAttribute() {
        return attributes;
    }
}
