package com.github.liebharc.queryenricher;

import java.util.Objects;

public class Attribute {

    private final String domain;
    private final String property;

    public Attribute(String domain, String property) {
        this.domain = domain;
        this.property = property;
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
}
