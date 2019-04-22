package com.github.liebharc.queryenricher;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PlanBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidSelectorsTest() {
        final List<Selector> selectors =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        InMemoryQueryBuilder.studentId);
        new InMemoryPlanBuilder(selectors);
    }

    @Test
    public void findSelectorsSimpleTest() {
        final List<Selector> selectors =
                Arrays.asList(
                    InMemoryQueryBuilder.studentId,
                    InMemoryQueryBuilder.firstName,
                    InMemoryQueryBuilder.lastName,
                    new SelectorBuilder().addAttribute("teacher", "id").addColumn("ID").build(),
                    new SelectorBuilder().addAttribute("teacher", "firstName").addColumn("FIRST_NAME").build(),
                    new SelectorBuilder().addAttribute("teacher", "lastName").addColumn("LAST_NAME").build());

        final PlanBuilder planBuilder = new InMemoryPlanBuilder(selectors);

        Plan plan = planBuilder.build(
                        Arrays.asList(InMemoryQueryBuilder.studentIdAttr,
                                InMemoryQueryBuilder.lastNameAttr,
                                InMemoryQueryBuilder.firstNameAttr));

        Assert.assertArrayEquals(plan.getSelectors().toArray(new Selector[0]), new Selector[] { selectors.get(0), selectors.get(2), selectors.get(1) });
    }

    @Test
    public void queryColumnsTest() {
        final List<Selector> selectors =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        InMemoryQueryBuilder.firstName,
                        InMemoryQueryBuilder.lastName);

        final PlanBuilder planBuilder = new InMemoryPlanBuilder(selectors);
        InMemoryQueryBuilder.database.setup(
                new Student(10, "David", "Tenant"),
                new Student(11, "Matt", "Smith"));
        Plan plan = planBuilder.build(Arrays.asList(
                InMemoryQueryBuilder.lastNameAttr,
                InMemoryQueryBuilder.firstNameAttr));
        QueryResult result = plan.getQuery().query();
    }
}