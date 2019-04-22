package com.github.liebharc.queryenrichment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class H2QueryBuilder implements QueryBuilder{


    public static final Selector<Long> studentId = new SelectorBuilder().addAttribute(Attributes.studentId).addColumn("ID").build();
    public static final Selector<String> firstName = new SelectorBuilder().addAttribute(Attributes.firstName).addColumn("FIRST_NAME").build();
    public static final Selector<String> lastName = new SelectorBuilder().addAttribute(Attributes.lastName).addColumn("LAST_NAME").build();
    public static final Selector<Long> studentClass = new SelectorBuilder().addAttribute(Attributes.studentClass).addColumn("CLASS").build();
    public static final Selector<String> fullName = new Enrichment<String>(Attributes.fullName) {
        @Override
        public void enrich(IntermediateResult result) {
            result.add(this, result.get(Attributes.firstName) + " " + result.get(Attributes.lastName));
        }

        @Override
        public List<Attribute<?>> getDependencies() {
            return Arrays.asList(Attributes.firstName, Attributes.lastName);
        }
    };

    private final Statement statement;

    public H2QueryBuilder(Statement statement) {
        this.statement = statement;
    }

    @Override
    public com.github.liebharc.queryenrichment.Query build(List<Selector<?>> selectors, String domain, List<SimpleExpression> filters) {
        if (selectors.isEmpty()) {
            throw  new IllegalArgumentException("At least one attribute must be selected");
        }

        String select = selectors.stream().flatMap(sel -> {
            Optional<String> column = sel.getColumn();
            if (column.isPresent()) {
                return Collections.singletonList(column.get()).stream();
            } else {
                final List<String> empty = Collections.emptyList();
                return empty.stream();
            }
        }).collect(Collectors.joining(", "));

        final StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(select);
        query.append(" FROM ");
        query.append(domain);

        if (!filters.isEmpty()) {
            query.append(" WHERE ");
            query.append(FilterUtils.getSqlCriteria(filters));
        }

        return new Query(query.toString(), selectors);
    }

    private class Query implements com.github.liebharc.queryenrichment.Query {

        private final String query;
        private final List<Selector<?>> selectors;

        public Query(String query, List<Selector<?>> selectors) {
            this.query = query;
            this.selectors = selectors;
        }

        @Override
        public QueryResult query() {
            try {
                ResultSet resultSet = statement.executeQuery(query);

                final List<List<Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    final List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= selectors.size(); i++) {
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
