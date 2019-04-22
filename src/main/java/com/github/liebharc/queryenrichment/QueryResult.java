package com.github.liebharc.queryenrichment;

import java.util.List;

public class QueryResult {
    private final List<List<Object>> rows;

    public QueryResult(List<List<Object>> rows) {
        this.rows = rows;
    }

    public List<List<Object>> getRows() {
        return rows;
    }
}
