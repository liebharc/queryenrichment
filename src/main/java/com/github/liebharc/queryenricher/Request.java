package com.github.liebharc.queryenricher;

import java.util.List;

public class Request {
    private final List<Attribute> attributes;

    public Request(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
