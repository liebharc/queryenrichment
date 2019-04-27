package com.github.liebharc.queryenrichment;

import java.util.*;

/**
 * Implements the topological sort of steps.
 */
public class TopologicalSort {

    /** Singleton */
    public static final TopologicalSort INSTANCE = new TopologicalSort();

    private TopologicalSort() {
        // Singleton
    }

    /**
     * Sorts steps according to their dependencies.
     * @param source Steps to be sorted
     * @param attributeToStep Allows to quickly find the step for an attribute.
     * @return Sorted steps
     */
    public List<Step<?>> sort(Collection<Step<?>> source, Map<Attribute<?>, Step<?>> attributeToStep) {
        final List<Step<?>> sorted = new ArrayList<>();
        final Set<Attribute<?>> visitedSet = new HashSet<>();

        for (Step<?> item : source) {
            this.visit(item, visitedSet, sorted, attributeToStep);
        }

        return sorted;
    }

    private void visit(Step<?> item, Set<Attribute<?>> visited, List<Step<?>> sorted, Map<Attribute<?>, Step<?>> attributeToSelector) {
        if (visited.contains(item.getAttribute())) {
            if (!sorted.contains(item)) {
                throw new IllegalArgumentException("Cyclic dependency found, stopped at " + item);
            }
        }
        else {
            visited.add(item.getAttribute());

            Dependency dependencies = item.getDependencies();
            for (Attribute<?> dependency : dependencies.getMinimalRequiredAttributes(visited)) {
                Step<?> stepDependency = attributeToSelector.get(dependency);
                if (stepDependency == null) {
                    throw new IllegalArgumentException("Unresolved dependency found. " + item + " requires " + dependency);
                }

                this.visit(stepDependency, visited, sorted, attributeToSelector);
            }


            sorted.add(item);
        }
    }
}
