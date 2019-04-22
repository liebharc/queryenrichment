package com.github.liebharc.queryenrichment;

import java.sql.Statement;
import java.util.List;

public class H2PlanBuilder extends PlanBuilder {

    private final Statement statement;

    public H2PlanBuilder(Statement statement, List<Selector> selectors) {
        super(selectors);
        this.statement = statement;
    }

    @Override
    protected QueryBuilder getQueryBuilder() {
        return new H2QueryBuilder(statement);
    }
}
