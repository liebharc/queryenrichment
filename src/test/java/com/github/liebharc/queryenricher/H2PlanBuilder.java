package com.github.liebharc.queryenricher;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class H2PlanBuilder extends PlanBuilder {

    private final Statement statement;

    public H2PlanBuilder(Statement statement, List<Selector> selectors) {
        super(selectors);
        this.statement = statement;
    }

    @Override
    protected QueryBuilder getQueryBuilder(Map<Selector, Integer> lookupTable) {
        return new H2QueryBuilder(lookupTable, statement);
    }
}
