package com.github.liebharc.queryenrichment;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PlanBuilder {
    private final Map<Attribute, Selector> attributeToSelector = new HashMap<Attribute, Selector>();

    public PlanBuilder(List<Selector> selectors) {
        final StringBuilder errorBuilder = new StringBuilder(0);

        for (Selector selector : selectors) {
            Selector previousMapping= this.attributeToSelector.put(selector.getAttribute(), selector);

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
        if (request.getAttributes().isEmpty()) {
            throw new IllegalArgumentException("At least one attribute must be requested");
        }

        if (this.hasMultipleDomains(request.getAttributes())) {
            throw new IllegalArgumentException("Can't query for multiple domain in one request");
        }

        String domain = request.getAttributes().get(0).getDomain();

        // IntermediateResult requires that all selectors which require a filter are first in the list, the order method
        // ensures that this holds true
        final List<Selector> selectors =
                this.orderSelectorsByDependencies(
                        this.addDependencies(
                            this.findRequiredSelectors(request)));
        final List<SimpleExpression> filters = this.translatePropertyNames(domain, request.getCriteria());
        final List<Selector> queryColumns = selectors.stream().filter(sel -> sel.getColumn().isPresent()).collect(Collectors.toList());
        return new Plan(request.getAttributes(), selectors, this.createLookupTable(request.getAttributes()), this.getQueryBuilder().build(queryColumns, domain, filters));
    }

    private boolean hasMultipleDomains(List<Attribute> attributes) {
        final String domain = attributes.get(0).getDomain();
        return attributes.stream().anyMatch(attr -> !attr.getDomain().equals(domain));
    }

    private List<Selector> findRequiredSelectors(Request request) {
        Map<String, SimpleExpression> equalityFilters = request.getCriteria().stream()
                .filter(expr -> this.isEqualityExpression(expr))
                .collect(Collectors.toMap(expr -> expr.getPropertyName(), expr -> expr));

        return request.getAttributes().stream().map(attr -> {
            SimpleExpression filterExpression = equalityFilters.get(attr.getProperty());
            if (filterExpression != null) {
                return new FromFilterEnrichment(attr, filterExpression);
            }
            else {
                return attributeToSelector.get(attr);
            }
        }).collect(Collectors.toList());
    }


    private List<Selector> addDependencies(List<Selector> requiredSelectors) {
        final List<Selector> result = new ArrayList<>();
        for (Selector selector : requiredSelectors) {
            this.addDependency(result, selector);
        }

        return result;
    }

    private void addDependency(List<Selector> result, Selector item) {
        if (result.contains(item)) {
            return;
        }

        result.add(item);

        for (Attribute dependency : item.getDependencies()) {
            this.addDependency(result, attributeToSelector.get(dependency));
        }
    }

    private List<Selector> orderSelectorsByDependencies(List<Selector> selectors) {
        Map<Boolean, List<Selector>> directColumns
                = selectors.stream().collect(Collectors.partitioningBy(sel -> sel.getColumn().isPresent()));
        return TopologicalSort.INSTANCE.sort(directColumns.get(false), directColumns.get(true), attributeToSelector);
    }

    private List<SimpleExpression> translatePropertyNames(String domain, List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            Attribute attribute = new Attribute(expr.getValue().getClass(), domain, expr.getPropertyName());
            Optional<String> selector = Optional.ofNullable(attributeToSelector.get(attribute)).flatMap(sel -> sel.getColumn());
            if (selector.isPresent()) {
                return new SimpleExpression(selector.get(), expr.getOperation(), expr.getValue());
            }
            else {
                return expr;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Indicates whether or not the given expression is an equality expression. Beware that the implementation
     * is a hack. If this project goes somewhere then we likely want to get rid of the whole Hibernate dependency
     * and have an own filter expression tree.
     */
    private boolean isEqualityExpression(SimpleExpression expr) {
        return expr.getOperation().equals("=");
    }

    private List<AttributePosition> createLookupTable(List<Attribute> attributes) {
        final List<AttributePosition> lookup = new ArrayList<>(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            lookup.add(new AttributePosition(attributes.get(i), i));
        }

        return lookup;
    }

    protected abstract QueryBuilder getQueryBuilder();
}
