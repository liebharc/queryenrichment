package com.github.liebharc.queryenrichment;

/**
 * A shortcut for a select where the column name matches the property name of the attribute.
 * @param <T> Attribute type
 */
public class PropertySelector<T> extends Selector<T> {
    public PropertySelector(Attribute<T> attribute, Dependency dependency) {
        super(attribute, attribute.getProperty(), dependency);
    }
}
