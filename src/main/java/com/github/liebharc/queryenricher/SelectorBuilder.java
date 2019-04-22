package com.github.liebharc.queryenricher;

import java.util.ArrayList;
import java.util.List;

public class SelectorBuilder {
    private final List<Attribute> attributes = new ArrayList<Attribute>();
    private final List<String> columns = new ArrayList<String>();

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public SelectorBuilder addAttribute(String domain, String property) {
        attributes.add(new Attribute(domain, property));
        return this;
    }

    public SelectorBuilder addColumn(String column) {
        columns.add(column);
        return this;
    }

    public Selector build() {
        return new Selector(attributes, columns);
    }
}
