package com.github.liebharc.queryenricher;

import java.util.List;

public class FilterUtils {

    public static String getSqlCriteria(List<SimpleExpression> criteria) {
        final StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (SimpleExpression criterion : criteria) {
            SimpleExpression expression = criterion;

            if (!isFirst) {
                result.append(" and ");
            }

            result.append(expression.toString());
            isFirst = false;
        }

        return result.toString();
    }

    private FilterUtils() {

    }
}
