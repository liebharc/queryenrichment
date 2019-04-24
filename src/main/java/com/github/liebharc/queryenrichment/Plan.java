package com.github.liebharc.queryenrichment;

import java.util.ArrayList;
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
            final List<Object[]> results = new ArrayList<>();
            final IntermediateResult intermediateResult = new IntermediateResult();

            // determine constants once at the beginning
            if (this.processConstants(intermediateResult)) {
                return new EnrichedQueryResult(attributes, results.toArray(new Object[0][]));
            }

            for (int i = 0; i < rows.size(); i++) {
                intermediateResult.nextRow(rows.get(i));
                this.processRow(results, intermediateResult);
            }

            return new EnrichedQueryResult(attributes, results.toArray(new Object[0][]));
        }
        finally {
            statistics.addTotal(System.currentTimeMillis() - start);
        }
    }

    private boolean processConstants(IntermediateResult intermediateResult) {
        for (Step<?> step : constants) {
            step.enrich(intermediateResult);
            if (!intermediateResult.isContinueProcessing()) {
                return true;
            }
        }

        intermediateResult.markCurrentResultAsConstant();
        return false;
    }

    private void processRow(List<Object[]> results, IntermediateResult intermediateResult) {
        for (Step<?> step : steps) {
            step.enrich(intermediateResult);
            if (!intermediateResult.isContinueProcessing()) {
                return;
            }
        }

        int pos = 0;
        Object[] row = new Object[attributes.size()];
        for (Attribute attribute : attributes) {
            row[pos] = intermediateResult.get(attribute);
            pos++;
        }

        results.add(row);
    }

    List<Step<?>> getSteps() {
        return steps;
    }
}
