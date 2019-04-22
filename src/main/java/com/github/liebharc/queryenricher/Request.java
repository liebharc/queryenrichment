package com.github.liebharc.queryenricher;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Request {
    private final List<Attribute> attributes;

    private final List<SimpleExpression> criteria;

    public Request(List<Attribute> attributes) {
        this(attributes, Collections.emptyList());
    }

    public Request(List<Attribute> attributes, List<SimpleExpression> criteria) {
        this.attributes = attributes;
        this.criteria = criteria;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<SimpleExpression> getCriteria() {
        return criteria;
    }

    public String getSqlCriteria() {
        final StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (SimpleExpression criterion : criteria) {
            SimpleExpression expression = criterion;

            if (!isFirst) {
                result.append(" and ");
            }

            result.append(expression.toString());
            isFirst = false;
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(attributes, request.attributes) &&
                Objects.equals(criteria, request.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, criteria);
    }
}
