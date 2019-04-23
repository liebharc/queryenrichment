package com.github.liebharc.queryenrichment;

import java.util.Collections;
import java.util.List;

public class Plan {
    private final List<Attribute<?>> attributes;
    private final List<Step<?>> constants;
    private final List<Step<?>> steps;
    private final Query query;
    private final ExecutionStatistics statistics = new ExecutionStatistics();

    public Plan(
            List<Attribute<?>> attributes,
            List<Step<?>> constants,
            List<Step<?>> steps,
            Query query) {
        this.attributes = attributes;
        this.constants = constants;
        this.steps = Collections.unmodifiableList(steps);
        this.query = query;
    }

    public EnrichedQueryResult execute(Request request) {
        long start = System.currentTimeMillis();
        try {
            final QueryResult queryResult = query.query(request);
            statistics.addQueryTime(System.currentTimeMillis() - start);
            final List<List<Object>> rows = queryResult.getRows();
            final Object[][] results = new Object[rows.size()][];
            final IntermediateResult intermediateResult = new IntermediateResult();
            boolean isFirstRow = true;
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = new Object[attributes.size()];
                intermediateResult.nextRow(rows.get(i));

                if (isFirstRow) {
                    // determine constants once
                    for (Step<?> step : constants) {
                        step.enrich(intermediateResult);
                    }

                    intermediateResult.markCurrentResultAsConstant();
                }

                for (Step<?> step : steps) {
                    step.enrich(intermediateResult);
                }

                int pos = 0;
                for (Attribute attribute : attributes) {
                    row[pos] = intermediateResult.get(attribute);
                    pos++;
                }

                results[i] = row;
                isFirstRow = false;
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
