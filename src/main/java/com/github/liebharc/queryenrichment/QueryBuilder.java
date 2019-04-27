package com.github.liebharc.queryenrichment;

import java.util.List;

/**
 * A query builder prepares a {@link Query}.
 */
public interface QueryBuilder {
    /**
     * Builds a query.
     * @param steps Select expression
     * @param filters FilterStep or where expression
     * @return Query object
     */
    Query build(List<Step<?>> steps, List<QueryFilter> filters);
}