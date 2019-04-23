package com.github.liebharc.queryenrichment;

public class SelectorBuilder<T> {
    private Attribute<T> attribute;
    private String column;

    public SelectorBuilder(Attribute<T> attribute) {
        this.attribute = attribute;
    }

    public SelectorBuilder<T> addColumn(String column) {
        this.column = column;
        return this;
    }

    public Step<T> build() {
        return new Selector<>(attribute, column);
    }
}
