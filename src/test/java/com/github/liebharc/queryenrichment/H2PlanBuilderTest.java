package com.github.liebharc.queryenrichment;

import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class H2PlanBuilderTest {

    private Connection connection;
    private Statement statement;

    @Before
    public void setupH2() throws SQLException {
        connection = DriverManager.
                getConnection("jdbc:h2:mem:test", "sa", "");
        statement = connection.createStatement();
        statement.execute("CREATE TABLE STUDENT ( ID int(11) NOT NULL, FIRST_NAME varchar(255), LAST_NAME varchar(255), CLASS int(11))");
        statement.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))");
        statement.execute("INSERT INTO CLASS(id, description) VALUES (1, 'Doctor for everything')");
        statement.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (10, 'David', 'Tenant', 1)");
        statement.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (11, 'Matt', 'Smith', 1)");
    }

    @After
    public void disposeH2() throws SQLException {
        statement.execute("DROP TABLE CLASS");
        statement.execute("DROP TABLE STUDENT");
        statement.close();
        connection.close();
    }

    @Test
    public void queryTest() {
        final List<Selector<?>> selectors = this.createDefaultSeletors();
        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.lastName,
                                Attributes.firstName)));

        final String stringResult = this.resultToString(plan.execute());
        Assert.assertEquals(
                "10,Tenant,David\n" +
                "11,Smith,Matt", stringResult);
    }

    @Test
    public void withCriteriaTest() {
        final List<Selector<?>> selectors = this.createDefaultSeletors();
        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final SimpleExpression criterion = SimpleExpression.neq("id", 11);
        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.lastName,
                                Attributes.firstName),
                        Arrays.asList(criterion)));

        final String stringResult = this.resultToString(plan.execute());
        Assert.assertEquals(
                "10,Tenant,David", stringResult);
    }

    @Test
    public void replaceSelectorByFilterTest() {
        final List<Selector<?>> selectors = this.createDefaultSeletors();
        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final SimpleExpression criterion = SimpleExpression.eq("firstName", "David");
        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.lastName,
                                Attributes.firstName),
                        Arrays.asList(criterion)));

        Assert.assertEquals(2, plan.getSelectors().stream().filter(sel -> !(sel instanceof FromFilterEnrichment)).count());
        final String stringResult = this.resultToString(plan.execute());
        Assert.assertEquals(
                "10,Tenant,David", stringResult);
    }

    @Test
    public void executeSimpleQueryTest() {
        final List<Selector<?>> selectors = this.createDefaultSeletors();
        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final SimpleExpression criterion = SimpleExpression.eq("firstName", "David");
        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.lastName,
                                Attributes.firstName),
                        Arrays.asList(criterion)));
        EnrichedQueryResult result = plan.execute();
        Assert.assertEquals(
                "10,Tenant,David", this.resultToString(result));
    }

    @Test
    public void enrichmentWithManualDependencyResolutionTest() {
        final List<Selector<?>> selectors = this.createDefaultSeletors();
        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.firstName,
                                Attributes.lastName,
                                Attributes.fullName)));
        EnrichedQueryResult result = plan.execute();
        Assert.assertEquals(
                "10,David,Tenant,David Tenant\n" +
                         "11,Matt,Smith,Matt Smith", this.resultToString(result));
    }

    @Test
    public void enrichmentWithAutomaticDependencyResolutionTest() {
        final List<Selector<?>> selectors = this.createDefaultSeletors();
        final PlanBuilder planBuilder = new H2PlanBuilder(statement, selectors);

        final Plan plan = planBuilder.build(
                new Request(
                        Arrays.asList(Attributes.studentId,
                                Attributes.fullName)));
        EnrichedQueryResult result = plan.execute();
        Assert.assertEquals(
                "10,David Tenant\n" +
                         "11,Matt Smith", this.resultToString(result));
    }

    private String resultToString(EnrichedQueryResult result) {
        return Arrays.stream(result.getResults())
                .map(row -> Arrays.stream(row)
                        .map(cell -> cell.toString()).collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n"));
    }

    private List<Selector<?>> createDefaultSeletors() {
        return Arrays.asList(
                H2QueryBuilder.studentId,
                H2QueryBuilder.firstName,
                H2QueryBuilder.lastName,
                H2QueryBuilder.fullName);
    }
}
