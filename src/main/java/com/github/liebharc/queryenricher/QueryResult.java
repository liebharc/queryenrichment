package com.github.liebharc.queryenricher;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private final List<Selector> selectors;
    private final List<List<Object>> rows;

    public QueryResult(Map<Selector, Integer> lookupTable, List<Selector> selectors, List<List<Object>> rows) {
        this.selectors = selectors;
        this.rows = rows;
    }

    List<Selector> getSelectors() {
        return selectors;
    }

    List<List<Object>> getRows() {
        return rows;
    }
}
