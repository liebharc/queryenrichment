package com.github.liebharc.queryenrichment;

public class DatabaseFilter {
    private final SimpleExpression filter;
    private final String column;

    public DatabaseFilter(SimpleExpression filter) {
        this(filter, filter.getAttribute().getProperty());
    }

    public DatabaseFilter(SimpleExpression filter, String column) {
        this.filter = filter;
        this.column = column;
    }

    public SimpleExpression getExpression() {
        return filter;
    }

    public String getColumn() {
        return column;
    }

    public String toPlaceHolderString() {
        return column + filter.getOperation() + "?";
    }
}
