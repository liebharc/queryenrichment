package com.github.liebharc.queryenrichment;

import java.util.*;

public class TopologicalSort {

    public static final TopologicalSort INSTANCE = new TopologicalSort();

    private TopologicalSort() {

    }

    public List<Step<?>> sort(Collection<Step<?>> source, Map<Attribute<?>, Step<?>> attributeToSelector) {
        final List<Step<?>> sorted = new ArrayList<>();
        final Set<Step<?>> visitedSet = new HashSet<>();

        for (Step<?> item : source) {
            this.visit(item, visitedSet, sorted, attributeToSelector);
        }

        return sorted;
    }

    private void visit(Step<?> item, Set<Step<?>> visited, List<Step<?>> sorted, Map<Attribute<?>, Step<?>> attributeToSelector) {
        if (visited.contains(item)) {
            if (!sorted.contains(item)) {
                throw new IllegalArgumentException("Cyclic dependency found, stopped at " + item);
            }
        }
        else {
            visited.add(item);

            Dependency dependencies = item.getDependenciesCached();
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
