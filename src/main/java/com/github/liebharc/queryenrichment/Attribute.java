package com.github.liebharc.queryenrichment;

import java.util.Objects;

public class Attribute<T> {

    private final Class<T> attributeClass;
    private final String domain;
    private final String property;

    public Attribute(Class<T> attributeClass, String domain, String property) {
        this.attributeClass = attributeClass;
        this.domain = domain;
        this.property = property;
    }

    public Class<T> getAttributeClass() {
        return attributeClass;
    }

    public String getDomain() {
        return domain;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return Objects.equals(domain, attribute.domain) &&
                Objects.equals(property, attribute.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, property);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "domain='" + domain + '\'' +
                ", property='" + property + '\'' +
                '}';
    }
}
