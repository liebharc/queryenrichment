package com.github.liebharc.queryenrichment;

import java.util.List;

public interface QueryBuilder {
    /**
     * Builds a query.
     * @param selectors Select expression
     * @param domain Domain from where the data should be collected
     * @param filters Filter or where expression
     * @return Query object
     */
    Query build(List<Selector> selectors, String domain, List<SimpleExpression> filters);
}