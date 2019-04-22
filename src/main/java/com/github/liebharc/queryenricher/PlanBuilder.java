package com.github.liebharc.queryenricher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class PlanBuilder {
    private final Map<Attribute, Selector> selectors = new HashMap<Attribute, Selector>();

    public PlanBuilder(List<Selector> selectors) {
        final StringBuilder errorBuilder = new StringBuilder(0);

        for (Selector selector : selectors) {
            for (Attribute attribute : selector.getAttributes()) {
                Selector previousMapping= this.selectors.put(attribute, selector);

                if (previousMapping != null) {
                    errorBuilder.append(attribute + "  has more than one selector; " + previousMapping + " and " + selector + "\n");
                }
            }
        }

        final String errors = errorBuilder.toString();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Failed to plan query:\n" + errors);
        }
    }

    public Plan build(List<Attribute> attributes) {
        List<Selector> selectors = this.findRequiredSelectors(attributes);
        return new Plan(selectors, this.getBuilder(this.createLookupTable(selectors)).build(selectors));
    }

    private List<Selector> findRequiredSelectors(List<Attribute> attributes) {
        return attributes.stream().map(attr -> selectors.get(attr)).collect(Collectors.toList());
    }

    private Map<Selector, Integer> createLookupTable(List<Selector> selectors) {
        final Map<Selector, Integer> lookup = new HashMap<>();
        for (int i = 0; i < selectors.size(); i++) {
            lookup.put(selectors.get(i), i);
        }

        return lookup;
    }

    protected abstract QueryBuilder getBuilder(Map<Selector, Integer> lookupTable);
}
