package com.github.liebharc.queryenricher;

import java.util.List;

public interface QueryBuilder {

    Query build(List<Selector> selectors);
}
