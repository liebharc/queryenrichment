package com.github.liebharc.queryenricher;

public abstract class Enrichment extends Selector {

    public Enrichment(Attribute attribute, String columnOrNull) {
        super(attribute, columnOrNull);
    }

    public abstract void enrich();
}
