package com.github.liebharc.queryenrichment;

import java.util.List;

public class EnrichedQueryResult {
    private final List<Attribute<?>> attributes;
    private final Object[][] results;

    public EnrichedQueryResult(List<Attribute<?>> attributes, Object[][] results) {
        this.attributes = attributes;
        this.results = results;
    }

    public List<Attribute<?>> getAttributes() {
        return attributes;
    }

    public Object[][] getResults() {
        return results;
    }
}
