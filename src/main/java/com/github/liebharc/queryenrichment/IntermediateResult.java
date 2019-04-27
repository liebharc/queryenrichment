package com.github.liebharc.queryenrichment;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class IntermediateResult {

    private List<Object> queryResult;

    private int queryResultPos = 0;

    private Map<Attribute, Object> results = new HashMap<>();

    private Map<Attribute, Object> constants = new HashMap<>();

    private boolean continueProcessing = true;

    public<T> void add(Step<T> step, T result) {
        results.put(step.getAttribute(), result);
    }

    public<T> void add(Attribute<T> attribute, T result) { results.put(attribute, result); }

    @SuppressWarnings("unchecked")
    public<T> T get(Attribute<T> attribute) {
        final T constant = (T) constants.get(attribute);
        if (constant != null) {
            return constant;
        }

        return (T)results.get(attribute);
    }

    @SuppressWarnings("unchecked")
    public<T> T getOrCreate(Attribute<T> attribute, Supplier<T> onMissSupplier) {
        final T result = this.get(attribute);
        if (result != null) {
            return result;
        }

        final T newInstance = onMissSupplier.get();
        this.add(attribute, newInstance);
        return newInstance;
    }

    public void addFromQuery(Step<?> step) {
        Attribute<?> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.cast(step.getAttribute().getAttributeClass(), queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    public void addLongFromQuery(Step<Long> step) {
        Attribute<Long> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castLong(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    public void addIntegerFromQuery(Step<Integer> step) {
        Attribute<Integer> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castInteger(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    public void addShortFromQuery(Step<Short> step) {
        Attribute<Short> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castShort(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    public void addBooleanFromQuery(Step<Boolean> step) {
        Attribute<Boolean> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castBoolean(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    public void addFloatFromQuery(Step<Float> step) {
        Attribute<Float> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castFloat(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    public void addDoubleFromQuery(Step<Double> step) {
        Attribute<Double> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castDouble(queryResult.get(queryResultPos)));
        this.nextColumn();
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
        continueProcessing = true;
    }

    public boolean isContinueProcessing() {
        return continueProcessing;
    }

    public void stopProcessing() {
        continueProcessing = false;
    }

    public void markCurrentResultAsConstant() {
        constants = results;
        results = new HashMap<>();
    }
}
