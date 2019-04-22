package com.github.liebharc.queryenrichment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class H2QueryBuilder implements QueryBuilder{

    public static final Selector studentId = new SelectorBuilder().addAttribute("student", "id").addColumn("ID").build();
    public static final Selector firstName = new SelectorBuilder().addAttribute("student", "firstName").addColumn("FIRST_NAME").build();
    public static final Selector lastName = new SelectorBuilder().addAttribute("student", "lastName").addColumn("LAST_NAME").build();

    public static final Attribute studentIdAttr = studentId.getAttribute();
    public static final Attribute firstNameAttr = firstName.getAttribute();
    public static final Attribute lastNameAttr = lastName.getAttribute();

    public static final Selector fullName = new Enrichment(new Attribute("student", "fullName")) {
        @Override
        public void enrich(IntermediateResult result) {
            result.add(this, result.get(firstName.getAttribute()) + " " + result.get(lastName.getAttribute()));
        }

        @Override
        public List<Attribute> getDependencies() {
            return Arrays.asList(firstName.getAttribute(), lastName.getAttribute());
        }
    };


    public static final Attribute fullNameAttr = fullName.getAttribute();

    public static final Selector studentClass = new SelectorBuilder().addAttribute("student", "class").addColumn("CLASS").build();

    private final Statement statement;

    public H2QueryBuilder(Statement statement) {
        this.statement = statement;
    }

    @Override
    public com.github.liebharc.queryenrichment.Query build(List<Selector> selectors, String domain, List<SimpleExpression> filters) {
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
        private final List<Selector> selectors;

        public Query(String query, List<Selector> selectors) {
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
