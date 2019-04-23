package com.github.liebharc.queryenrichment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryPlanBuilderTest {

    @Before
    public void resetInMemoryDb() {
        InMemoryQueryBuilder.database.clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSelectorsTest() {
        final List<Step<?>> steps =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        InMemoryQueryBuilder.studentId);
        new InMemoryPlanBuilder(steps);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleDomainsTest() {
        final List<Step<?>> steps =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        new SelectorBuilder<>(Attributes.teacherId).addColumn("ID").build());
        InMemoryPlanBuilder planBuilder = new InMemoryPlanBuilder(steps);
        planBuilder.build(new Request(steps.stream().map(Step::getAttribute).collect(Collectors.toList())));
    }

    @Test
    public void findSelectorsSimpleTest() {
        final List<Step<?>> steps =
                Arrays.asList(
                    InMemoryQueryBuilder.studentId,
                    InMemoryQueryBuilder.firstName,
                    InMemoryQueryBuilder.lastName,
                    new SelectorBuilder<>(Attributes.teacherId).addColumn("ID").build(),
                    new SelectorBuilder<>(Attributes.teacherFirstName).addColumn("FIRST_NAME").build(),
                    new SelectorBuilder<>(Attributes.teacherLastName).addColumn("LAST_NAME").build());

        final PlanBuilder planBuilder = new InMemoryPlanBuilder(steps);

        final Plan plan = planBuilder.build(
                        new Request(
                            Arrays.asList(Attributes.studentId,
                                    Attributes.lastName,
                                    Attributes.firstName)));

        Assert.assertArrayEquals(plan.getSteps().toArray(new Step<?>[0]), new Step<?>[] { steps.get(0), steps.get(2), steps.get(1) });
    }

    @Test
    public void planCacheTest() {
        final List<Step<?>> steps =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        InMemoryQueryBuilder.firstName,
                        InMemoryQueryBuilder.lastName);

        final PlanBuilder planBuilder = new InMemoryPlanBuilder(steps);
        final PlanCache planCache = new PlanCache(10, planBuilder);
        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName));

        final Plan plan1 = planCache.getOrBuildPlan(request);
        final Plan plan2 = planCache.getOrBuildPlan(request);
        Assert.assertSame(plan1, plan2);
    }
}