package com.github.liebharc.queryenrichment;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * A step which is executed to get the result for a query.
 * @param <T>
 */
public interface Step<T> extends Serializable {

    /** Constant which can be passed if a step has no direct relation to a column/property */
    String NO_COLUMN = null;

    /** Returns the related column/property name for a query if there is one */
    Optional<String> getColumn();

    /** Returns the attribute which is set during this step */
    Attribute<T> getAttribute();

    /**
     * This main method of a step. Works with the result object to get the values from dependencies and sets the
     * value of this step.
     * @param result Gives access to the results of other steps and allows this step to store its results.
     */
    void enrich(IntermediateResult result);

    /**
     * Gets the dependencies of this step.
     */
    Dependency getDependencies();

    /**
     * Indicates whether or not the step is constant. Constant steps are steps which will return the same value for all
     * rows in a query.
     */
    boolean isConstant();
}
