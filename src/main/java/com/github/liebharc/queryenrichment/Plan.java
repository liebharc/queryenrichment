package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;

public class Plan {
    private final List<Attribute<?>> attributes;
    private final List<Step<?>> steps;
    private final List<AttributePosition> lookupTable;
    private final Query query;
    private final ExecutionStatistics statistics = new ExecutionStatistics();

    public Plan(List<Attribute<?>> attributes, List<Step<?>> steps, List<AttributePosition> lookupTable, Query query) {
        this.attributes = attributes;
        this.steps = Collections.unmodifiableList(steps);
        this.lookupTable = lookupTable;
        this.query = query;
    }

    public EnrichedQueryResult execute() {
        long start = System.currentTimeMillis();
        try {
            final QueryResult queryResult = query.query();
            statistics.addQueryTime(System.currentTimeMillis() - start);
            final List<List<Object>> rows = queryResult.getRows();
            final Object[][] results = new Object[rows.size()][];
            final IntermediateResult intermediateResult = new IntermediateResult();
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = new Object[attributes.size()];
                intermediateResult.nextRow(rows.get(i));

                for (Step<?> step : steps) {
                    step.enrich(intermediateResult);
                }

                for (AttributePosition attributePosition : lookupTable) {
                    row[attributePosition.getPosition()] = intermediateResult.get(attributePosition.getAttribute());
                }

                results[i] = row;
            }

            return new EnrichedQueryResult(attributes, results);
        }
        finally {
            statistics.addTotal(System.currentTimeMillis() - start);
        }
    }

    List<Step<?>> getSteps() {
        return steps;
    }
}
