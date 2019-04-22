package com.github.liebharc.queryenricher;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private final List<List<Object>> rows;

    public QueryResult(List<List<Object>> rows) {
        this.rows = rows;
    }

    List<List<Object>> getRows() {
        return rows;
    }
}
