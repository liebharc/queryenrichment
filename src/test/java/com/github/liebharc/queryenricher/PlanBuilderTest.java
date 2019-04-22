package com.github.liebharc.queryenricher;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PlanBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidSelectors() {
        final List<Selector> selectors =
                Arrays.asList(
                        new SelectorBuilder().addAttribute("student", "id").addColumn("ID").build(),
                        new SelectorBuilder().addAttribute("student", "id").addColumn("ID").build());
        new PlanBuilder(selectors);
    }

    @Test
    public void findSelectorsSimple() {
        final List<Selector> selectors =
                Arrays.asList(
                    new SelectorBuilder().addAttribute("student", "id").addColumn("ID").build(),
                    new SelectorBuilder().addAttribute("student", "firstName").addColumn("FIRST_NAME").build(),
                    new SelectorBuilder().addAttribute("student", "lastName").addColumn("LAST_NAME").build(),
                    new SelectorBuilder().addAttribute("teacher", "id").addColumn("ID").build(),
                    new SelectorBuilder().addAttribute("teacher", "firstName").addColumn("FIRST_NAME").build(),
                    new SelectorBuilder().addAttribute("teacher", "lastName").addColumn("LAST_NAME").build());

        final PlanBuilder planBuilder = new PlanBuilder(selectors);

        Plan plan = planBuilder.build(
                        Arrays.asList(new Attribute("student", "id"),
                        new Attribute("student", "lastName"),
                        new Attribute("student", "firstName")));

        Assert.assertArrayEquals(plan.getSelectors().toArray(new Selector[0]), new Selector[] { selectors.get(0), selectors.get(2), selectors.get(1) });
    }
}