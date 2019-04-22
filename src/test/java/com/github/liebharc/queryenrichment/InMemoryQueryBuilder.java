package com.github.liebharc.queryenrichment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryQueryBuilder implements QueryBuilder {
    public static final Database database = new Database();

    public static final Selector<Long> studentId = new SelectorBuilder<>(Attributes.studentId).addColumn("ID").build();
    public static final Selector<String> firstName = new SelectorBuilder<>(Attributes.firstName).addColumn("FIRST_NAME").build();
    public static final Selector<String> lastName = new SelectorBuilder<>(Attributes.lastName).addColumn("LAST_NAME").build();

    public InMemoryQueryBuilder() {
    }

    @Override
    public com.github.liebharc.queryenrichment.Query build(List<Selector<?>> selectors, String domain, List<SimpleExpression> filters) {
        if (!filters.isEmpty()) {
            throw new IllegalArgumentException("This class doesn't support criteria");
        }

        return new Query(selectors);
    }

    public static class Database {
        public final List<Student> students = new ArrayList<>();

        public void clear() {
            students.clear();
        }

        public void add(Student student) {
            students.add(student);
        }

        public void setup(Student... students) {
            this.clear();
            for (Student student : students) {
                this.add(student);
            }

        }
    }

    public class Query implements com.github.liebharc.queryenrichment.Query {

        private final List<Selector<?>> selectors;

        public Query(List<Selector<?>> selectors) {

            this.selectors = selectors;
        }

        @Override
        public QueryResult query() {
            List<List<Object>> rows = database.students.stream().map(student ->
                    selectors.stream().map(selector -> {
                        if (selector.equals(studentId)) {
                            return (Object)student.getId();
                        } else if (selector.equals(firstName)) {
                            return (Object)student.getFirstName();
                        } else if (selector.equals(lastName)) {
                            return (Object)student.getLastName();
                        }

                        throw new IllegalArgumentException("Unknown column " + selector);
                    }).collect(Collectors.toList())).collect(Collectors.toList());
            return new QueryResult(rows);
        }
    }
}
