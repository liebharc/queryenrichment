package com.github.liebharc.queryenrichment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A plan holds an execution plan which consists of a query and steps. It allows to execute the plan which results
 * in a {@link EnrichedQueryResult}.
 */
public class Plan {
    /** Ordered list of attributes which are queried */
    private final List<Attribute<?>> attributes;
    /** Ordered list of constant steps */
    private final List<Step<?>> constants;
    /** Ordered list of per row steps */
    private final List<Step<?>> steps;
    /** Query for this plan */
    private final Query query;
    /** Execution statistics */
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
                if (this.processRow(intermediateResult)) {
                    results.add(this.storeResultInObjectArray(intermediateResult));
                }
            }

            return new EnrichedQueryResult(attributes, results.toArray(new Object[0][]));
        }
        finally {
            statistics.addTotal(System.currentTimeMillis() - start);
        }
    }

    /**
     * Executes all constants steps and then marks the result as constant.
     */
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

    /**
     * Processes all per row steps. Returns false if the row should be filtered out.
     */
    private boolean processRow(IntermediateResult intermediateResult) {
        for (Step<?> step : steps) {
            step.enrich(intermediateResult);
            if (!intermediateResult.isContinueProcessing()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Stores the results in an object array.
     */
    private Object[] storeResultInObjectArray(IntermediateResult intermediateResult) {
        int pos = 0;
        Object[] row = new Object[attributes.size()];
        for (Attribute attribute : attributes) {
            row[pos] = intermediateResult.get(attribute);
            pos++;
        }

        return row;
    }

    List<Step<?>> getSteps() {
        return steps;
    }
}
