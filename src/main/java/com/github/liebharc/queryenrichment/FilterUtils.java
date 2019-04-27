package com.github.liebharc.queryenrichment;

import java.util.List;

public class FilterUtils {

    public static String getSqlCriteria(List<DatabaseFilter> criteria) {
        final StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (DatabaseFilter criterion : criteria) {

            if (!isFirst) {
                result.append(" and ");
            }

            result.append(criterion.toPlaceHolderString());
            isFirst = false;
        }

        return result.toString();
    }

    private FilterUtils() {

    }
}
