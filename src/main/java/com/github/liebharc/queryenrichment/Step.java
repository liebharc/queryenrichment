package com.github.liebharc.queryenrichment;

import java.util.List;
import java.util.Optional;

public interface Step<T> {

    String NO_COLUMN = null;

    Optional<String> getColumn();

    Attribute<T> getAttribute();

    void enrich(IntermediateResult result);

    List<Attribute<?>> getDependencies();

    boolean isConstant();
}
