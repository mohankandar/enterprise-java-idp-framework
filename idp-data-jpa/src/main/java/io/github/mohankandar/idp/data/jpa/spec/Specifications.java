package io.github.mohankandar.idp.data.jpa.spec;

import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public final class Specifications {
    private Specifications() {}

    public static <T> Specification<T> eq(String field, Object value) {
        return (root, q, cb) -> (value == null)
                ? cb.conjunction()
                : cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> in(String field, Collection<?> values) {
        return (root, q, cb) -> {
            if (values == null || values.isEmpty()) return cb.conjunction();
            return root.get(field).in(values);
        };
    }

    public static <T> Specification<T> like(String field, String pattern) {
        return (root, q, cb) -> (pattern == null || pattern.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get(field)), "%" + pattern.toLowerCase() + "%");
    }

    public static <T, Y extends Comparable<? super Y>>
    Specification<T> ge(String field, Y value) {
        return (root, q, cb) -> (value == null)
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.<Y>get(field), value);
    }

    public static <T, Y extends Comparable<? super Y>>
    Specification<T> le(String field, Y value) {
        return (root, q, cb) -> (value == null)
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.<Y>get(field), value);
    }

    public static <T, Y extends Comparable<? super Y>>
    Specification<T> between(String field, Y from, Y to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.<Y>get(field), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.<Y>get(field), from);
            return cb.lessThanOrEqualTo(root.<Y>get(field), to);
        };
    }
}
