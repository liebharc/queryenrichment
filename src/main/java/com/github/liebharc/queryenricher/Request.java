package com.github.liebharc.queryenricher;

import java.util.List;
import java.util.Objects;

public class Request {
    private final List<Attribute> attributes;

    public Request(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(attributes, request.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }
}
