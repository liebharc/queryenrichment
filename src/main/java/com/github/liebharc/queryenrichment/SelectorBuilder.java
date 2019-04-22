package com.github.liebharc.queryenrichment;

public class SelectorBuilder {
    private Attribute attribute;
    private String column;

    public void addAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public SelectorBuilder addAttribute(String domain, String property) {
        this.attribute = new Attribute(domain, property);
        return this;
    }

    public SelectorBuilder addColumn(String column) {
        this.column = column;
        return this;
    }

    public Selector build() {
        return new Selector(attribute, column);
    }
}
