package com.github.liebharc.queryenrichment;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PlanBuilder {
    private final Map<Attribute, Step<?>> attributeToSelector = new HashMap<>();

    public PlanBuilder(List<Step<?>> steps) {
        final StringBuilder errorBuilder = new StringBuilder(0);

        for (Step<?> step : steps) {
            Step<?> previousMapping= this.attributeToSelector.put(step.getAttribute(), step);

            if (previousMapping != null) {
                errorBuilder.append(step.getAttribute())
                        .append("  has more than one step; ")
                        .append(previousMapping).append(" and ")
                        .append(step).append("\n");
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

        // IntermediateResult requires that all steps which require a filter are first in the list, the order method
        // ensures that this holds true
        final List<Step<?>> steps =
                this.orderSelectorsByDependencies(
                        this.addDependencies(
                            this.findRequiredSelectors(request)));
        final List<SimpleExpression> filters = this.translatePropertyNames(domain, request.getCriteria());
        final List<Step<?>> queryColumns = steps.stream().filter(sel -> sel.getColumn().isPresent()).collect(Collectors.toList());
        return new Plan(request.getAttributes(), steps, this.getQueryBuilder().build(queryColumns, domain, filters));
    }

    private boolean hasMultipleDomains(List<Attribute<?>> attributes) {
        final String domain = attributes.get(0).getDomain();
        return attributes.stream().anyMatch(attr -> !attr.getDomain().equals(domain));
    }

    private List<Step<?>> findRequiredSelectors(Request request) {
        Map<String, SimpleExpression> equalityFilters = request.getCriteria().stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getPropertyName, expr -> expr));

        return request.getAttributes().stream().map(attr -> {
            SimpleExpression filterExpression = equalityFilters.get(attr.getProperty());
            if (filterExpression != null) {
                return new FromFilterEnrichment<>(attr, filterExpression);
            }
            else {
                return attributeToSelector.get(attr);
            }
        }).collect(Collectors.toList());
    }


    private List<Step<?>> addDependencies(List<Step<?>> requiredSteps) {
        final List<Step<?>> result = new ArrayList<>();
        for (Step<?> step : requiredSteps) {
            this.addDependency(result, step);
        }

        return result;
    }

    private void addDependency(List<Step<?>> result, Step<?> item) {
        if (result.contains(item)) {
            return;
        }

        result.add(item);

        for (Attribute<?> dependency : item.getDependencies()) {
            this.addDependency(result, attributeToSelector.get(dependency));
        }
    }

    private List<Step<?>> orderSelectorsByDependencies(List<Step<?>> steps) {
        return TopologicalSort.INSTANCE.sort(steps, attributeToSelector);
    }

    private List<SimpleExpression> translatePropertyNames(String domain, List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            final Attribute<?> attribute = new Attribute<>(expr.getValue().getClass(), domain, expr.getPropertyName());
            final Optional<String> selector = Optional.ofNullable(attributeToSelector.get(attribute)).flatMap(Step::getColumn);
            return selector.map(s -> new SimpleExpression(s, expr.getOperation(), expr.getValue())).orElse(expr);
        }).collect(Collectors.toList());
    }

    /**
     * Indicates whether or not the given expression is an equality expression.
     */
    private boolean isEqualityExpression(SimpleExpression expr) {
        return expr.getOperation().equals("=");
    }

    protected abstract QueryBuilder getQueryBuilder();
}
