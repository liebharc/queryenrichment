package com.github.liebharc.queryenricher;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SchemaDocumentationTest {

    @Test
    public void drawSchemaTest() {
        final String schema = SchemaDocumentation.INSTANCE.drawSchema(Arrays.asList(
                H2QueryBuilder.studentIdAttr,
                H2QueryBuilder.firstNameAttr,
                H2QueryBuilder.lastNameAttr,
                H2QueryBuilder.studentClass.getAttribute(),
                H2QueryBuilder.fullNameAttr,
                new Attribute("teacher", "id"),
                new Attribute("teacher", "firstName"),
                new Attribute("teacher", "lastName"),
                new Attribute("teacher", "class"),
                new Attribute("class", "id"),
                new Attribute("class", "description")));
        final String expected =
                "class:\n" +
                "\t - description\n" +
                "\t - id\n" +
                "student:\n" +
                "\t - class\n" +
                "\t - firstName\n" +
                "\t - fullName\n" +
                "\t - id\n" +
                "\t - lastName\n" +
                "teacher:\n" +
                "\t - class\n" +
                "\t - firstName\n" +
                "\t - id\n" +
                "\t - lastName\n";
        Assert.assertEquals(expected, schema);
    }
}