package com.github.liebharc.queryenrichment;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PlanBuilder {
    private final Map<Attribute<?>, Step<?>> attributeToSelector = new HashMap<>();

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

        final String domain = request.getAttributes().get(0).getDomain();

        final Map<Boolean, List<SimpleExpression>> groupedByQueryFilter = this.groupByQueryFilter(request.getCriteria());
        final List<Step<?>> filterSteps =
                this.addDependencies(
                        this.createFilterSteps(
                            domain,
                            groupedByQueryFilter.getOrDefault(false, Collections.emptyList())));
        List<SimpleExpression> sqlQueryExpressions = groupedByQueryFilter.getOrDefault(true, Collections.emptyList());
        final List<Step<?>> allRequiredSteps =
                this.addStepsForFilters(filterSteps,
                    this.injectConstants(sqlQueryExpressions,
                        this.addDependencies(
                            this.findRequiredSelectors(sqlQueryExpressions, request))));
        final List<Step<?>> orderedSteps = this.orderSelectorsByDependencies(allRequiredSteps);
        final List<SimpleExpression> filters =
                this.translatePropertyNames(
                        domain,
                        groupedByQueryFilter.getOrDefault(true, Collections.emptyList()));
        final List<Step<?>> queryColumns =
                orderedSteps.stream()
                        .filter(sel -> sel.getColumn().isPresent()).collect(Collectors.toList());
        final Map<Boolean, List<Step<?>>> groupedByConstant = this.groupByConstant(orderedSteps);
        return new Plan(
                request.getAttributes(),
                groupedByConstant.get(true),
                groupedByConstant.get(false),
                this.getQueryBuilder().build(queryColumns, domain, filters));
    }

    private List<Step<?>> addStepsForFilters(List<Step<?>> filterSteps, List<Step<?>> requiredSelectors) {
        final List<Step<?>> result = new ArrayList<>(filterSteps.size() + requiredSelectors.size());
        result.addAll(filterSteps);
        for (Step<?> selector : requiredSelectors) {
            if (!result.contains(selector)) {
                result.add(selector);
            }
        }

        return result;
    }

    private List<Step<?>> createFilterSteps(String domain, List<SimpleExpression> javaFilters) {
        return javaFilters.stream().map(expr -> {
            final Attribute<Object> attribute = new Attribute<>(Object.class, domain, expr.getPropertyName());
            final Step<?> step = attributeToSelector.get(attribute);
            if (step == null) {
                throw new IllegalArgumentException("Failed to find selector for expression " + expr);
            }

            return Filter.createFilter(step, expr);
        }).collect(Collectors.toList());
    }

    private Map<Boolean, List<Step<?>>> groupByConstant(List<Step<?>> steps) {
        final List<Step<?>> constant = new ArrayList<>();
        final Set<Attribute<?>> constantAttributes = new HashSet<>();
        final List<Step<?>> notConstant = new ArrayList<>();
        for (Step<?> step : steps) {
            if (step.isConstant() && step.getDependencies().isEmpty()) {
                constant.add(step);
                constantAttributes.add(step.getAttribute());
            }
            else if (!step.getColumn().isPresent() && step.getDependencies().stream().allMatch(s -> constantAttributes.contains(s))) {
                constant.add(step);
                constantAttributes.add(step.getAttribute());
            }
            else {
                notConstant.add(step);
            }
        }

        final Map<Boolean, List<Step<?>>> result = new HashMap<>();
        result.put(false, notConstant);
        result.put(true, constant);
        return result;
    }

    private boolean hasMultipleDomains(List<Attribute<?>> attributes) {
        final String domain = attributes.get(0).getDomain();
        return attributes.stream().anyMatch(attr -> !attr.getDomain().equals(domain));
    }

    private List<Step<?>> injectConstants(List<SimpleExpression> queryFilters, List<Step<?>> steps) {
        Map<String, SimpleExpression> equalityFilters = queryFilters.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getPropertyName, expr -> expr));

        return steps.stream().map(step -> {
            if (step.isConstant()) {
                return step;
            }

            SimpleExpression filterExpression = equalityFilters.get(step.getAttribute().getProperty());
            if (filterExpression != null) {
                return new AddValuesFromFilter<>(step.getAttribute(), filterExpression);
            }
            else {
                return step;
            }
        }).collect(Collectors.toList());
    }

    private List<Step<?>> findRequiredSelectors(List<SimpleExpression> queryExpressions, Request request) {
        Map<String, SimpleExpression> equalityFilters = queryExpressions.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getPropertyName, expr -> expr));

        return request.getAttributes().stream().map(attr -> {
            SimpleExpression filterExpression = equalityFilters.get(attr.getProperty());
            if (filterExpression != null) {
                return new AddValuesFromFilter<>(attr, filterExpression);
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
            final Step<?> step = attributeToSelector.get(dependency);
            if (step == null) {
                throw new IllegalArgumentException("Inconsistent selector tree, a selector contains an dependency which doesn't exist: " + item + " requires " + dependency);
            }

            this.addDependency(result, step);
        }
    }

    private List<Step<?>> orderSelectorsByDependencies(List<Step<?>> steps) {
        final Map<Attribute<?>, Step<?>> attributeToSelectorsWithConstants = new HashMap<>(attributeToSelector);
        for (Step<?> step : steps) {
            if (step.isConstant()) {
                attributeToSelectorsWithConstants.put(step.getAttribute(), step);
            }
        }

        return TopologicalSort.INSTANCE.sort(steps, attributeToSelectorsWithConstants);
    }

    private List<SimpleExpression> translatePropertyNames(String domain, List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            final Attribute<?> attribute = new Attribute<>(expr.getValue().getClass(), domain, expr.getPropertyName());
            final Optional<String> selector = Optional.ofNullable(attributeToSelector.get(attribute)).flatMap(Step::getColumn);
            return selector.map(s -> new SimpleExpression(s, expr.getOperation(), expr.getValue())).orElse(expr);
        }).collect(Collectors.toList());
    }

    private Map<Boolean, List<SimpleExpression>> groupByQueryFilter(List<SimpleExpression> criteria) {
        return criteria.stream().collect(Collectors.groupingBy(this::isSupportedByQuery));
    }

    protected boolean isSupportedByQuery(SimpleExpression criteria) {
        return true;
    }

    /**
     * Indicates whether or not the given expression is an equality expression.
     */
    private boolean isEqualityExpression(SimpleExpression expr) {
        return expr.getOperation().equals("=");
    }

    protected abstract QueryBuilder getQueryBuilder();
}
