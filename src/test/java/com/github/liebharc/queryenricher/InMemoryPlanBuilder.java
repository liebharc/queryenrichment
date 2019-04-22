package com.github.liebharc.queryenricher;

import java.util.List;
import java.util.Map;

public class InMemoryPlanBuilder extends PlanBuilder {

    public InMemoryPlanBuilder(List<Selector> selectors) {
        super(selectors);
    }

    @Override
    protected QueryBuilder getQueryBuilder(Map<Selector, Integer> lookupTable) {
        return new InMemoryQueryBuilder(lookupTable);
    }
}
