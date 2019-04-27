package com.github.liebharc.queryenrichment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a plan for a query. This class is intended to be subclassed so that implementors can provide their
 * query builder.
 */
public abstract class PlanBuilder {
    /** Maps attributes to the step which creates that attribute */
    private final Map<Attribute<?>, Step<?>> attributeToStep = new HashMap<>();

    public PlanBuilder(List<Step<?>> steps) {

        // Create lookup tables and check steps
        final StringBuilder errorBuilder = new StringBuilder(0);
        for (Step<?> step : steps) {
            Step<?> previousMapping= this.attributeToStep.put(step.getAttribute(), step);

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

    /**
     * Builds the plan for a request.
     */
    public Plan build(Request request) {
        if (request.getAttributes().isEmpty()) {
            throw new IllegalArgumentException("At least one attribute must be requested");
        }

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
                            this.findRequiredSteps(sqlQueryExpressions, request))));
        final List<Step<?>> orderedSteps = this.orderSelectorsByDependencies(allRequiredSteps);
        final List<QueryFilter> filters =
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
                this.getQueryBuilder().build(queryColumns, filters));
    }

    /**
     * Joins the lists of selector/enrichment and filter steps.
     */
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

    /**
     * Creates Java filters for the given filter expressions.
     */
    private List<Step<?>> createFilterSteps(List<SimpleExpression> javaFilters) {
        return javaFilters.stream().map(expr -> {
            final Step<?> step = attributeToStep.get(expr.getAttribute());
            if (step == null) {
                throw new IllegalArgumentException("Failed to find selector for expression " + expr);
            }

            return FilterStep.createFilter(step, expr);
        }).collect(Collectors.toList());
    }

    /**
     * Groups the given list of steps in constant/not-constant.
     */
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

    /**
     * Replaces steps by constants where possible.
     */
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

    /**
     * Find the required steps to implement the given list of filters in Java.
     */
    private List<Step<?>> findRequiredSteps(List<SimpleExpression> queryExpressions, Request request) {
        Map<Attribute<?>, SimpleExpression> equalityFilters = queryExpressions.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getAttribute, expr -> expr));

        return request.getAttributes().stream().map(attr -> {
            SimpleExpression filterExpression = equalityFilters.get(attr);
            if (filterExpression != null) {
                return new AddValuesFromFilter<>(attr, filterExpression);
            }
            else {
                return attributeToStep.get(attr);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Adds the dependencies for all steps.
     */
    private List<Step<?>> addDependencies(List<Step<?>> requiredSteps) {
        final List<Step<?>> result = new ArrayList<>();
        final Set<Attribute<?>> availableAttributes =
                requiredSteps.stream().map(step -> step.getAttribute()).collect(Collectors.toSet());
        for (Step<?> step : requiredSteps) {
            this.addDependency(result, step, availableAttributes);
        }

        return result;
    }

    /**
     * Adds the dependencies for a step.
     */
    private void addDependency(List<Step<?>> result, Step<?> item, Set<Attribute<?>> availableAttributes) {
        if (result.contains(item)) {
            return;
        }

        result.add(item);
        availableAttributes.add(item.getAttribute());

        for (Attribute<?> dependency : item.getDependencies().getMinimalRequiredAttributes(availableAttributes)) {
            final Step<?> step = attributeToStep.get(dependency);
            if (step == null) {
                throw new IllegalArgumentException("Inconsistent selector tree, a selector contains an dependency which doesn't exist: " + item + " requires " + dependency);
            }

            this.addDependency(result, step, availableAttributes);
        }
    }

    /**
     * Creates a linear execution plan for given list of steps.
     */
    private List<Step<?>> orderSelectorsByDependencies(List<Step<?>> steps) {
        final Map<Attribute<?>, Step<?>> attributeToSelectorsWithConstants = new HashMap<>(attributeToStep);
        for (Step<?> step : steps) {
            if (step.isConstant()) {
                attributeToSelectorsWithConstants.put(step.getAttribute(), step);
            }
        }

        return TopologicalSort.INSTANCE.sort(steps, attributeToSelectorsWithConstants);
    }

    /**
     * Adds the column names to a filter expression.
     */
    private List<QueryFilter> translatePropertyNames(List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            final Optional<String> selector = Optional.ofNullable(attributeToStep.get(expr.getAttribute())).flatMap(Step::getColumn);
            return selector.map(s -> new QueryFilter(expr, s)).orElse(new QueryFilter(expr));
        }).collect(Collectors.toList());
    }

    /**
     * Groups filters into one of two groups: Filters which can be executed together with the query and filters
     * which must be executed in Java.
     */
    private Map<Boolean, List<SimpleExpression>> groupByQueryFilter(List<SimpleExpression> criteria) {
        return criteria.stream().collect(Collectors.groupingBy(this::isSupportedByQuery));
    }

    /**
     * Indicates whether or not the given expression is an equality expression.
     */
    private boolean isEqualityExpression(SimpleExpression expr) {
        return expr.getOperation().equals("=");
    }

    /**
     * Intended to be overwritten. Indicates whether or not an expression can be added to the query.
     */
    protected boolean isSupportedByQuery(SimpleExpression criteria) {
        return true;
    }

    /**
     * Intended to be overwritten. Provides the concrete query builder which should be used.
     */
    protected abstract QueryBuilder getQueryBuilder();
}
