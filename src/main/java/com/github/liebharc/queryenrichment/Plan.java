package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Plan {
    private final List<Attribute> attributes;
    private final List<Selector> selectors;
    private final List<AttributePosition> lookupTable;
    private final Query query;
    private final ExecutionStatistics statistics = new ExecutionStatistics();

    public Plan(List<Attribute> attributes, List<Selector> selectors, List<AttributePosition> lookupTable, Query query) {
        this.attributes = attributes;
        this.selectors = Collections.unmodifiableList(selectors);
        this.lookupTable = lookupTable;
        this.query = query;
    }

    public EnrichedQueryResult execute() {
        long start = System.currentTimeMillis();
        try {
            final QueryResult queryResult = query.query(); final List<List<Object>> rows = queryResult.getRows();
            final Object[][] results = new Object[rows.size()][];
            final IntermediateResult intermediateResult = new IntermediateResult();
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = new Object[attributes.size()];
                intermediateResult.nextRow(rows.get(i));

                for (Selector selector : selectors) {
                    selector.enrich(intermediateResult);
                    intermediateResult.nextColumn();
                }

                for (AttributePosition attributePosition : lookupTable) {
                    row[attributePosition.getPosition()] = intermediateResult.get(attributePosition.getAttribute());
                }

                results[i] = row;
            }

            return new EnrichedQueryResult(attributes, results);
        }
        finally {
            statistics.add(System.currentTimeMillis() - start);
        }
    }

    List<Selector> getSelectors() {
        return selectors;
    }
}
