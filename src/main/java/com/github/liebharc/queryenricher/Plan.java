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
        final QueryResult queryResult = query.query();
        final List<List<Object>> rows = queryResult.getRows();
        final Object[][] results = new Object[rows.size()][];
        final IntermediateResult intermediateResult = new IntermediateResult();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = new Object[attributes.size()];
            intermediateResult.nextRow(rows.get(i));

            for (Selector selector : selectors) {
                selector.enrich(intermediateResult);
                intermediateResult.nextColumn();
            }

            for (Attribute attribute : attributes) {
                row[lookupTable.get(attribute)] = intermediateResult.get(attribute);
            }

            results[i] = row;
        }

        return new EnrichedQueryResult(attributes, results);
    }

    List<Selector> getSelectors() {
        return selectors;
    }
}
