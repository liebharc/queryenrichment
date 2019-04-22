package com.github.liebharc.queryenricher;

import java.util.List;

public abstract class Enricher extends Selector {

    public Enricher(List<Attribute> attributes, List<String> columns) {
        super(attributes, columns);
    }

    public abstract void enrich();
}
