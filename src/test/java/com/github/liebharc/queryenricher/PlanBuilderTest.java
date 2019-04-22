package com.github.liebharc.queryenricher;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PlanBuilderTest {

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

        Assert.assertNotNull(plan);
    }
}