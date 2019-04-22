package com.github.liebharc.queryenricher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntermediateResult {

    private List<Object> queryResult;

    private int queryResultPos = 0;

    private final Map<Attribute, Object> results = new HashMap<>();

    public void add(Selector selector, Object result) {
        results.put(selector.getAttribute(), result);
    }

    public Object get(Attribute attribute) {
        return results.get(attribute);
    }

    public void addFromQuery(Selector selector) {
        // Plan builder ensures that we can rely on the query result position here
        results.put(selector.getAttribute(), queryResult.get(queryResultPos));
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
