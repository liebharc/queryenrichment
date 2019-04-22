package com.github.liebharc.queryenrichment;

import java.util.Objects;

/**
 * Inspired by Hibernate, needs to be fleshed out before it is useful.
 */
public class SimpleExpression {
    public static SimpleExpression eq(String propertyName, Object value) {
        return new SimpleExpression(propertyName, "=", value);
    }

    public static SimpleExpression neq(String propertyName, Object value) {
        return new SimpleExpression(propertyName, "!=", value);
    }

    private final String propertyName;
    private final String operation;
    private final Object value;


    public SimpleExpression(String propertyName, String operation, Object value) {
        this.propertyName = propertyName;
        this.operation = operation;
        this.value = value;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleExpression that = (SimpleExpression) o;
        return Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyName, operation, value);
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            return propertyName + operation + "'" + value + "'";
        }

        return propertyName + operation + value;
    }
}
