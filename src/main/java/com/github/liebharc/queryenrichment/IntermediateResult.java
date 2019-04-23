package com.github.liebharc.queryenrichment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntermediateResult {

    private List<Object> queryResult;

    private int queryResultPos = 0;

    private Map<Attribute, Object> results = new HashMap<>();

    private Map<Attribute, Object> constants = new HashMap<>();

    public<T> void add(Step step, T result) {
        results.put(step.getAttribute(), result);
    }

    @SuppressWarnings("unchecked")
    public<T> T get(Attribute<T> attribute) {
        final T constant = (T) constants.get(attribute);
        if (constant != null) {
            return constant;
        }

        return (T)results.get(attribute);
    }

    public void addFromQuery(Step<?> step) {
        // Plan builder ensures that we can rely on the query result position here
        Attribute<?> attribute = step.getAttribute();
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

    public void markCurrentResultAsConstant() {
        constants = results;
        results = new HashMap<>();
    }
}
