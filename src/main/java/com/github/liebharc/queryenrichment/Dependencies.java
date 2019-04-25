package com.github.liebharc.queryenrichment;

import java.util.*;

public class Dependencies {
    private static final NoDependency noDependency = new NoDependency();

    public static Dependency noDependencies() {
        return noDependency;
    }

    public static Dependency requireAll(Attribute<?>... attributes) {
        return new RequireAll(Arrays.asList(attributes));
    }

    public static Dependency require(Attribute<?> attribute) {
        return new RequireAll(Collections.singletonList(attribute));
    }

    public static Dependency requireOneOf(Attribute<?>... attributes) {
        return new RequireOneOf(Arrays.asList(attributes));
    }

    private static class RequireOneOf implements Dependency {

        private final Collection<Attribute<?>> attributes;

        private RequireOneOf(Collection<Attribute<?>> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Step<?>> available) {
            if (this.isEmpty()) {
                return Collections.emptyList();
            }

            final Optional<Attribute<?>> any = attributes.stream().filter(req -> available.stream().anyMatch(a -> a.getAttribute().equals(req))).findAny();
            if (any.isPresent()) {
                return Collections.singletonList(any.get());
            } else {
                // We have no match at all, inform the caller about one of the dependencies as this is the minimum
                // we require
                return Collections.singletonList(attributes.iterator().next());
            }
        }

        @Override
        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        @Override
        public boolean isOkay(Set<Attribute<?>> available) {
            if (this.isEmpty()) {
                return true;
            }

            return this.attributes.stream().anyMatch(req -> available.contains(req));
        }
    }


    private static class RequireAll implements Dependency {

        private final Collection<Attribute<?>> attributes;

        private RequireAll(Collection<Attribute<?>> attributes) {

            this.attributes = attributes;
        }

        @Override
        public Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Step<?>> available) {
            return this.attributes;
        }

        @Override
        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        @Override
        public boolean isOkay(Set<Attribute<?>> available) {
            return this.attributes.stream().allMatch(req -> available.contains(req));
        }
    }

    private static class NoDependency implements Dependency {

        @Override
        public Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Step<?>> available) {
            return Collections.emptyList();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isOkay(Set<Attribute<?>> constantAttributes) {
            return true;
        }
    }
}
