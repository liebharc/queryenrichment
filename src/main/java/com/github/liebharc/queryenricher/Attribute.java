package com.github.liebharc.queryenricher;

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
}
