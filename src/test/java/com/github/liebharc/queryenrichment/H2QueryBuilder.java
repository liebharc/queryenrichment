package com.github.liebharc.queryenrichment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class H2QueryBuilder implements QueryBuilder {

    public static int classIdStringCalls = 0;

    public static final Step<Long> studentId = new SelectorBuilder<>(Attributes.studentId).addColumn("ID").build();
    public static final Step<String> firstName = new SelectorBuilder<>(Attributes.firstName).addColumn("FIRST_NAME").build();
    public static final Step<String> lastName = new SelectorBuilder<>(Attributes.lastName).addColumn("LAST_NAME").build();
    public static final Step<Long> studentClass = new SelectorBuilder<>(Attributes.studentClass).addColumn("CLASS").build();
    public static final Step<String> fullName = new Enrichment<String>(Attributes.fullName) {
        @Override
        public void enrich(IntermediateResult result) {
            result.add(this, result.get(Attributes.firstName) + " " + result.get(Attributes.lastName));
        }

        @Override
        public Dependency getDependencies() {
            return Dependencies.requireAll(Attributes.firstName, Attributes.lastName);
        }
    };

    public static final Step<String> classIdString = new Enrichment<String>(Attributes.classIdString) {
        @Override
        public void enrich(IntermediateResult result) {
            classIdStringCalls++;
            result.add(this, "Class: " + result.get(Attributes.studentClass));
        }

        @Override
        public Dependency getDependencies() {
            return Dependencies.require(Attributes.studentClass);
        }
    };

    private final Connection connection;

    H2QueryBuilder(Connection connection) {
        this.connection = connection;
    }

    @Override
    public com.github.liebharc.queryenrichment.Query build(List<Step<?>> steps, String domain, List<SimpleExpression> filters) {
        final String select = this.createSelectStatement(steps);
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(select);
        query.append(" FROM ");
        query.append(domain);

        if (!filters.isEmpty()) {
            query.append(" WHERE ");
            query.append(FilterUtils.getSqlCriteria(filters));
        }

        try {
            return new Query(connection.prepareStatement(query.toString()), steps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String createSelectStatement(List<Step<?>> steps) {
        if (steps.isEmpty()) {
            return "1";
        }

        return steps.stream().flatMap(sel -> {
            Optional<String> column = sel.getColumn();
            if (column.isPresent()) {
                return Stream.of(column.get());
            } else {
                final List<String> empty = Collections.emptyList();
                return empty.stream();
            }
        }).collect(Collectors.joining(", "));
    }

    private class Query implements com.github.liebharc.queryenrichment.Query {

        private final PreparedStatement query;
        private final List<Step<?>> steps;

        Query(PreparedStatement query, List<Step<?>> steps) {
            this.query = query;
            this.steps = steps;
        }

        @Override
        public QueryResult query(Request request) {
            try {
                int pos = 1;
                for (SimpleExpression criterion : request.getCriteria()) {
                    query.setObject(pos, criterion.getValue());
                    pos++;
                }

                ResultSet resultSet = query.executeQuery();

                final List<List<Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    final List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= steps.size(); i++) {
                        row.add(resultSet.getObject(i));
                    }

                    results.add(row);
                }

                return new QueryResult(results);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
