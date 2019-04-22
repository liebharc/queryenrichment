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
            Selector previousMapping= this.selectors.put(selector.getAttribute(), selector);

            if (previousMapping != null) {
                errorBuilder.append(selector.getAttribute() + "  has more than one selector; " + previousMapping + " and " + selector + "\n");
            }
        }

        final String errors = errorBuilder.toString();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Failed to plan query:\n" + errors);
        }
    }

    public Plan build(Request request) {
        if (this.hasMultipleDomains(request.getAttributes())) {
            throw new IllegalArgumentException("Can't query for multiple domain in one request");
        }

        List<Selector> selectors = this.findRequiredSelectors(request.getAttributes());
        List<Selector> queryColumns = selectors.stream().filter(sel -> sel.getColumn().isPresent()).collect(Collectors.toList());
        return new Plan(selectors, this.getQueryBuilder(request, this.createLookupTable(selectors)).build(request, queryColumns));
    }

    private boolean hasMultipleDomains(List<Attribute> attributes) {
        if (attributes.isEmpty()) {
            return false;
        }

        final String domain = attributes.get(0).getDomain();
        return attributes.stream().anyMatch(attr -> !attr.getDomain().equals(domain));
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

    protected abstract QueryBuilder getQueryBuilder(Request request, Map<Selector, Integer> lookupTable);
}
