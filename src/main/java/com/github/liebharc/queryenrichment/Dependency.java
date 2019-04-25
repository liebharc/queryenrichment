package com.github.liebharc.queryenrichment;

import java.util.Collection;
import java.util.Set;

public interface Dependency {
    Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Step<?>> available);

    boolean isEmpty();

    boolean isOkay(Set<Attribute<?>> constantAttributes);
}
