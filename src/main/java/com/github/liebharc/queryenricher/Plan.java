package com.github.liebharc.queryenricher;

import java.util.Collections;
import java.util.List;

public class Plan {
    private final List<Selector> selectors;

    public Plan(List<Selector> selectors) {
        this.selectors = Collections.unmodifiableList(selectors);
    }

    public List<Selector> getSelectors() {
        return selectors;
    }
}
