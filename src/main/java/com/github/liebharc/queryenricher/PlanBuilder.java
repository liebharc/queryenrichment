package com.github.liebharc.queryenricher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        if (this.getDomain(request.getAttributes())) {
            throw new IllegalArgumentException("Can't query for multiple domain in one request");
        }

        String domain = request.getAttributes().get(0).getDomain();

        List<Selector> selectors = this.findRequiredSelectors(request);
        List<SimpleExpression> filters = this.translatePropertyNames(domain, request.getCriteria());
        List<Selector> queryColumns = selectors.stream().filter(sel -> sel.getColumn().isPresent()).collect(Collectors.toList());
        return new Plan(request.getAttributes(), selectors, this.createLookupTable(selectors), this.getQueryBuilder().build(filters, queryColumns));
    }

    private boolean getDomain(List<Attribute> attributes) {
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

    private List<SimpleExpression> translatePropertyNames(String domain, List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            Attribute attribute = new Attribute(domain, expr.getPropertyName());
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

    private Map<Attribute, Integer> createLookupTable(List<Selector> selectors) {
        final Map<Attribute, Integer> lookup = new HashMap<>();
        for (int i = 0; i < selectors.size(); i++) {
            lookup.put(selectors.get(i).getAttribute(), i);
        }

        return lookup;
    }

    protected abstract QueryBuilder getQueryBuilder();
}
