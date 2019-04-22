package com.github.liebharc.queryenricher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryQueryBuilder implements QueryBuilder {
    public static final Database database = new Database();

    public static final Selector studentId = new SelectorBuilder().addAttribute("student", "id").addColumn("ID").build();
    public static final Selector firstName = new SelectorBuilder().addAttribute("student", "firstName").addColumn("FIRST_NAME").build();
    public static final Selector lastName = new SelectorBuilder().addAttribute("student", "lastName").addColumn("LAST_NAME").build();

    public static final Attribute studentIdAttr = studentId.getAttributes().get(0);
    public static final Attribute firstNameAttr = firstName.getAttributes().get(0);
    public static final Attribute lastNameAttr = lastName.getAttributes().get(0);

    private final Map<Selector, Integer> lookupTable;

    public InMemoryQueryBuilder(Map<Selector, Integer> lookupTable) {

        this.lookupTable = lookupTable;
    }

    @Override
    public com.github.liebharc.queryenricher.Query build(List<Selector> selectors) {
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

    public class Query implements com.github.liebharc.queryenricher.Query {

        private final List<Selector> selectors;

        public Query(List<Selector> selectors) {

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

                        return (Object)null;
                    }).collect(Collectors.toList())).collect(Collectors.toList());
            return new QueryResult(lookupTable, selectors, rows);
        }
    }
}
