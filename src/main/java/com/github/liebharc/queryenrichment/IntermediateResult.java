package com.github.liebharc.queryenrichment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntermediateResult {

    private List<Object> queryResult;

    private int queryResultPos = 0;

    private final Map<Attribute, Object> results = new HashMap<>();

    public<T> void add(Selector selector, T result) {
        results.put(selector.getAttribute(), result);
    }

    @SuppressWarnings("unchecked")
    public<T> T get(Attribute<T> attribute) {
        return (T)results.get(attribute);
    }

    public void addFromQuery(Selector<?> selector) {
        // Plan builder ensures that we can rely on the query result position here
        Attribute<?> attribute = selector.getAttribute();
        results.put(attribute, this.cast(attribute.getAttributeClass(), queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Cast the value to the given class. Also supports the most crucial widening primitive conversions.
     */
    private Object cast(Class<?> clazz, Object value) {
        if (clazz == Long.class) {
            if (value instanceof Long) {
                return value;
            }

            if (value instanceof Integer) {
                return (long) ((int) value);
            }

            if (value instanceof Short) {
                return (long) ((short) value);
            }
        }

        if (clazz == Integer.class) {
            if (value instanceof Integer) {
                return value;
            }

            if (value instanceof Short) {
                return (int) ((short) value);
            }
        }

        if (clazz == Double.class && value instanceof Float) {
            return (double)((float)value);
        }

        return clazz.cast(value);
    }

    private void nextColumn() {
        queryResultPos++;
    }

    public void nextRow(List<Object> row) {
        this.clear();
        this.queryResult = row;
    }

    private void clear() {
        results.clear();
        queryResultPos = 0;
    }
}
