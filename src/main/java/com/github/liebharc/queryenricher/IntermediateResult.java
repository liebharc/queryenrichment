package com.github.liebharc.queryenricher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntermediateResult {

    private List<Object> queryResult;

    private int queryResultPos = 0;

    private final Map<Selector, Object> results = new HashMap<>();

    public void add(Selector selector, Object result) {
        results.put(selector, result);
    }

    public Object get(Selector selector) {
        return results.get(selector);
    }

    public void addFromQuery(Selector selector) {
        results.put(selector, queryResult.get(queryResultPos));
    }

    public void nextColumn() {
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
