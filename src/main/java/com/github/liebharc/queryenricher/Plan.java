package com.github.liebharc.queryenricher;

import java.util.Collections;
import java.util.List;

public class Plan {
    private final List<Selector> selectors;
    private final Query query;

    public Plan(List<Selector> selectors, Query query) {
        this.selectors = Collections.unmodifiableList(selectors);
        this.query = query;
    }

    List<Selector> getSelectors() {
        return selectors;
    }

    Query getQuery() {
        return query;
    }
}
