package com.github.liebharc.queryenrichment;

import java.util.List;

public class FilterUtils {

    public static String getSqlCriteria(List<SimpleExpression> criteria) {
        final StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (SimpleExpression criterion : criteria) {

            if (!isFirst) {
                result.append(" and ");
            }

            result.append(criterion.toString());
            isFirst = false;
        }

        return result.toString();
    }

    private FilterUtils() {

    }
}
