package com.github.liebharc.queryenrichment;

public class AttributePosition {
    private final Attribute attribute;
    private final int position;

    public AttributePosition(Attribute attribute, int position) {
        this.attribute = attribute;
        this.position = position;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public int getPosition() {
        return position;
    }
}
