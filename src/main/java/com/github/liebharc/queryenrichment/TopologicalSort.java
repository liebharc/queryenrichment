package com.github.liebharc.queryenrichment;

import java.util.*;

public class TopologicalSort {

    public static final TopologicalSort INSTANCE = new TopologicalSort();

    private TopologicalSort() {

    }

    public List<Selector> sort(Collection<Selector> source, Collection<Selector> visited, Map<Attribute, Selector> attributeToSelector) {
        final List<Selector> sorted = new ArrayList<>(visited);
        final Set<Selector> visitedSet = new HashSet<>(visited);

        for (Selector item : source) {
            this.visit(item, visitedSet, sorted, attributeToSelector);
        }

        return sorted;
    }

    private void visit(Selector item, Set<Selector> visited, List<Selector> sorted, Map<Attribute, Selector> attributeToSelector) {
        if (visited.contains(item)) {
            if (!sorted.contains(item)) {
                throw new IllegalArgumentException("Cyclic dependency found, stopped at " + item);
            }
        }
        else {
            visited.add(item);

            List<Attribute<?>> dependencies = item.getDependencies();
            for (Attribute dependency : dependencies) {
                Selector selectorDependency = attributeToSelector.get(dependency);
                if (selectorDependency == null) {
                    throw new IllegalArgumentException("Unresolved dependency found. " + item + " requires " + dependency);
                }

                this.visit(selectorDependency, visited, sorted, attributeToSelector);
            }


            sorted.add(item);
        }
    }
}
