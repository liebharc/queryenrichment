package com.github.liebharc.queryenricher;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaDocumentation {

    public static final SchemaDocumentation INSTANCE = new SchemaDocumentation();

    private SchemaDocumentation() {

    }

    public String drawSchema(List<Attribute> attributes) {
        final StringBuilder result = new StringBuilder();
        final Map<String, List<Attribute>> byDomain =
                attributes.stream().collect(Collectors.groupingBy(attribute -> attribute.getDomain()));
        byDomain.keySet().stream().sorted().forEach(domain -> {
            result.append(domain);
            result.append(":");
            result.append("\n");
            byDomain.get(domain).stream().map(attr -> attr.getProperty()).sorted().forEach(prop -> {
                result.append("\t");
                result.append(" - ");
                result.append(prop);
                result.append("\n");
            });
        });

        return result.toString();
    }
}
