package com.github.liebharc.queryenricher;

public abstract class Enricher extends Selector {

    public Enricher(Attribute attribute, String columnOrNull) {
        super(attribute, columnOrNull);
    }

    public abstract void enrich();
}
