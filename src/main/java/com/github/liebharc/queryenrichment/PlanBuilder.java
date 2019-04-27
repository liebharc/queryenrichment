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

        final String domain = request.getAttributes().get(0).getDomain();

        final Map<Boolean, List<SimpleExpression>> groupedByQueryFilter = this.groupByQueryFilter(request.getCriteria());
        final List<Step<?>> filterSteps =
                this.addDependencies(
                        this.createFilterSteps(
                                groupedByQueryFilter.getOrDefault(false, Collections.emptyList())));
        List<SimpleExpression> sqlQueryExpressions = groupedByQueryFilter.getOrDefault(true, Collections.emptyList());
        final List<Step<?>> allRequiredSteps =
                this.addStepsForFilters(filterSteps,
                    this.injectConstants(sqlQueryExpressions,
                        this.addDependencies(
                            this.findRequiredSelectors(sqlQueryExpressions, request))));
        final List<Step<?>> orderedSteps = this.orderSelectorsByDependencies(allRequiredSteps);
        final List<DatabaseFilter> filters =
                this.translatePropertyNames(
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

    private List<Step<?>> createFilterSteps(List<SimpleExpression> javaFilters) {
        return javaFilters.stream().map(expr -> {
            final Step<?> step = attributeToSelector.get(expr.getAttribute());
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
            else if (!step.getColumn().isPresent() && step.getDependencies().isOkay(constantAttributes)) {
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

    private List<Step<?>> injectConstants(List<SimpleExpression> queryFilters, List<Step<?>> steps) {
        Map<Attribute<?>, SimpleExpression> equalityFilters = queryFilters.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getAttribute, expr -> expr));

        return steps.stream().map(step -> {
            if (step.isConstant()) {
                return step;
            }

            SimpleExpression filterExpression = equalityFilters.get(step.getAttribute());
            if (filterExpression != null) {
                return new AddValuesFromFilter<>(step.getAttribute(), filterExpression);
            }
            else {
                return step;
            }
        }).collect(Collectors.toList());
    }

    private List<Step<?>> findRequiredSelectors(List<SimpleExpression> queryExpressions, Request request) {
        Map<Attribute<?>, SimpleExpression> equalityFilters = queryExpressions.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getAttribute, expr -> expr));

        return request.getAttributes().stream().map(attr -> {
            SimpleExpression filterExpression = equalityFilters.get(attr);
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
        final Set<Attribute<?>> availableAttributes =
                requiredSteps.stream().map(step -> step.getAttribute()).collect(Collectors.toSet());
        for (Step<?> step : requiredSteps) {
            this.addDependency(result, step, availableAttributes);
        }

        return result;
    }

    private void addDependency(List<Step<?>> result, Step<?> item, Set<Attribute<?>> availableAttributes) {
        if (result.contains(item)) {
            return;
        }

        result.add(item);
        availableAttributes.add(item.getAttribute());

        for (Attribute<?> dependency : item.getDependencies().getMinimalRequiredAttributes(availableAttributes)) {
            final Step<?> step = attributeToSelector.get(dependency);
            if (step == null) {
                throw new IllegalArgumentException("Inconsistent selector tree, a selector contains an dependency which doesn't exist: " + item + " requires " + dependency);
            }

            this.addDependency(result, step, availableAttributes);
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

    private List<DatabaseFilter> translatePropertyNames(List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            final Optional<String> selector = Optional.ofNullable(attributeToSelector.get(expr.getAttribute())).flatMap(Step::getColumn);
            return selector.map(s -> new DatabaseFilter(expr, s)).orElse(new DatabaseFilter(expr));
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
