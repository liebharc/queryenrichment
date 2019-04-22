package com.github.liebharc.queryenricher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Plan {
    private final List<Attribute> attributes;
    private final List<Selector> selectors;
    private final Map<Attribute, Integer> lookupTable;
    private final Query query;

    public Plan(List<Attribute> attributes, List<Selector> selectors, Map<Attribute, Integer> lookupTable, Query query) {
        this.attributes = attributes;
        this.selectors = Collections.unmodifiableList(selectors);
        this.lookupTable = lookupTable;
        this.query = query;
    }

    public EnrichedQueryResult execute() {
        QueryResult queryResult = this.getQuery().query();
        List<List<Object>> rows = queryResult.getRows();
        Object[][] results = new Object[rows.size()][];
        final IntermediateResult intermediateResult = new IntermediateResult();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = new Object[attributes.size()];
            intermediateResult.nextRow(rows.get(i));

            for (Selector selector : selectors) {
                selector.enrich(intermediateResult);
                intermediateResult.nextColumn();
            }

            for (Map.Entry<Attribute, Integer> selection : lookupTable.entrySet()) {
                row[selection.getValue()] = intermediateResult.get(selection.getKey());
            }

            results[i] = row;
        }

        return new EnrichedQueryResult(attributes, results);
    }

    List<Selector> getSelectors() {
        return selectors;
    }

    Query getQuery() {
        return query;
    }
}
