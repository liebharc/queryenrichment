package com.github.liebharc.queryenrichment;

public class SelectorBuilder<T> {
    private Attribute<T> attribute;
    private String column;

    public SelectorBuilder addAttribute(Attribute<T> attribute) {
        this.attribute = attribute;
        return this;
    }

    public SelectorBuilder addColumn(String column) {
        this.column = column;
        return this;
    }

    public Selector<T> build() {
        return new Selector<>(attribute, column);
    }
}
