package com.github.liebharc.queryenrichment;

import java.util.List;

public class InMemoryPlanBuilder extends PlanBuilder {

    public InMemoryPlanBuilder(List<Selector> selectors) {
        super(selectors);
    }

    @Override
    protected QueryBuilder getQueryBuilder() {
        return new InMemoryQueryBuilder();
    }
}
