package com.github.liebharc.queryenrichment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntermediateResult {

    private List<Object> queryResult;

    private int queryResultPos = 0;

    private final Map<Attribute, Object> results = new HashMap<>();

    public<T> void add(Selector selector, T result) {
        results.put(selector.getAttribute(), result);
    }

    public<T> T get(Attribute<T> attribute) {
        return (T)results.get(attribute);
    }

    public void addFromQuery(Selector<?> selector) {
        // Plan builder ensures that we can rely on the query result position here
        results.put(selector.getAttribute(), queryResult.get(queryResultPos));
        this.nextColumn();
    }

    private void nextColumn() {
        queryResultPos++;
    }

    public void nextRow(List<Object> row) {
        this.clear();
        this.queryResult = row;
    }

    public void clear() {
        results.clear();
        queryResultPos = 0;
    }
}
