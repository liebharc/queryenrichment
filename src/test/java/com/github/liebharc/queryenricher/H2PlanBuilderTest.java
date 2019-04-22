package com.github.liebharc.queryenricher;

import org.h2.tools.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class H2PlanBuilderTest {

    private Connection connection;
    private Statement statement;

    @Before
    public void setupH2() throws Exception {
        connection = DriverManager.
                getConnection("jdbc:h2:mem:test", "sa", "");
        statement = connection.createStatement();
        statement.execute("CREATE TABLE STUDENT ( ID int(11) NOT NULL, FIRST_NAME varchar(255), LAST_NAME varchar(255), CLASS int(11))");
        statement.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))");
        statement.execute("INSERT INTO CLASS(id, description) VALUES (1, 'Doctor for everything')");
        statement.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (10, 'David', 'Tenant', 1)");
        statement.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (11, 'Matt', 'Smith', 1)");
    }

    @Test
    public void queryTest() {
        final List<Selector> selectors =
                Arrays.asList(
                        H2QueryBuilder.studentId,
                        H2QueryBuilder.firstName,
                        H2QueryBuilder.lastName);

        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(InMemoryQueryBuilder.studentIdAttr,
                                InMemoryQueryBuilder.lastNameAttr,
                                InMemoryQueryBuilder.firstNameAttr)));

        final QueryResult queryResult = plan.getQuery().query();
        final String stringResult =
                queryResult.getRows().stream()
                        .map(row -> row.stream()
                            .map(cell -> cell.toString()).collect(Collectors.joining(",")))
                        .collect(Collectors.joining("\n"));
        Assert.assertEquals(
                "10,Tenant,David\n" +
                "11,Smith,Matt", stringResult);
    }
}
