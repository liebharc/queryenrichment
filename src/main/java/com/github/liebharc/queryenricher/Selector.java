package com.github.liebharc.queryenricher;

import java.util.Collections;
import java.util.List;

public class Selector {

    private final List<Attribute> attributes;
    private final List<String> columns;

    public Selector(List<Attribute> attributes, List<String> columns) {
        this.attributes = Collections.unmodifiableList(attributes);
        this.columns = Collections.unmodifiableList(columns);
    }

    List<String> getColumns() {
        return columns;
    }

    List<Attribute> getAttributes() {
        return attributes;
    }
}
