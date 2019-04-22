package com.github.liebharc.queryenrichment;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryPlanBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidSelectorsTest() {
        final List<Selector<?>> selectors =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        InMemoryQueryBuilder.studentId);
        new InMemoryPlanBuilder(selectors);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleDomainsTest() {
        final List<Selector<?>> selectors =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        new SelectorBuilder().addAttribute(Attributes.teacherId).addColumn("ID").build());
        InMemoryPlanBuilder planBuilder = new InMemoryPlanBuilder(selectors);
        planBuilder.build(new Request(selectors.stream().map(sel -> sel.getAttribute()).collect(Collectors.toList())));
    }

    @Test
    public void findSelectorsSimpleTest() {
        final List<Selector<?>> selectors =
                Arrays.asList(
                    InMemoryQueryBuilder.studentId,
                    InMemoryQueryBuilder.firstName,
                    InMemoryQueryBuilder.lastName,
                    new SelectorBuilder().addAttribute(Attributes.teacherId).addColumn("ID").build(),
                    new SelectorBuilder().addAttribute(Attributes.teacherFirstName).addColumn("FIRST_NAME").build(),
                    new SelectorBuilder().addAttribute(Attributes.teacherLastName).addColumn("LAST_NAME").build());

        final PlanBuilder planBuilder = new InMemoryPlanBuilder(selectors);

        final Plan plan = planBuilder.build(
                        new Request(
                            Arrays.asList(Attributes.studentId,
                                    Attributes.lastName,
                                    Attributes.firstName)));

        Assert.assertArrayEquals(plan.getSelectors().toArray(new Selector<?>[0]), new Selector<?>[] { selectors.get(0), selectors.get(2), selectors.get(1) });
    }

    @Test
    public void planCacheTest() {
        final List<Selector<?>> selectors =
                Arrays.asList(
                        InMemoryQueryBuilder.studentId,
                        InMemoryQueryBuilder.firstName,
                        InMemoryQueryBuilder.lastName);

        final PlanBuilder planBuilder = new InMemoryPlanBuilder(selectors);
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