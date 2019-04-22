package com.github.liebharc.queryenricher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class H2QueryBuilder implements QueryBuilder{

    public static final Selector studentId = new SelectorBuilder().addAttribute("student", "id").addColumn("ID").build();
    public static final Selector firstName = new SelectorBuilder().addAttribute("student", "firstName").addColumn("FIRST_NAME").build();
    public static final Selector lastName = new SelectorBuilder().addAttribute("student", "lastName").addColumn("LAST_NAME").build();
    public static final Selector studentClass = new SelectorBuilder().addAttribute("student", "class").addColumn("CLASS").build();

    private final Map<Selector, Integer> lookupTable;
    private final Statement statement;

    public H2QueryBuilder(Map<Selector, Integer> lookupTable, Statement statement) {
        this.lookupTable = lookupTable;

        this.statement = statement;
    }

    @Override
    public com.github.liebharc.queryenricher.Query build(Request request, List<Selector> selectors) {
        if (selectors.isEmpty()) {
            return ReturnNothingQuery.INSTANCE;
        }

        String domain = selectors.get(0).getAttribute().getDomain();

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

        if (!request.getCriteria().isEmpty()) {
            query.append(" WHERE ");
            query.append(request.getSqlCriteria());
        }

        return new Query(query.toString(), selectors);
    }

    private class Query implements com.github.liebharc.queryenricher.Query {

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

                return new QueryResult(lookupTable, selectors, results);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
