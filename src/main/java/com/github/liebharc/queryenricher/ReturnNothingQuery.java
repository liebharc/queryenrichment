package com.github.liebharc.queryenricher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ReturnNothingQuery implements Query {

    public static final ReturnNothingQuery INSTANCE =  new ReturnNothingQuery();

    private final QueryResult nothing = new QueryResult(Collections.emptyList());

    private ReturnNothingQuery() {

    }

    @Override
    public QueryResult query() {
        return nothing;
    }
}
