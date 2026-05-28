package io.github.mohankandar.idp.core.paging;

public final class SortOrder {
    public enum Direction { ASC, DESC }

    private final String field;
    private final Direction direction;

    private SortOrder(String field, Direction direction) {
        if (field == null || field.isBlank()) throw new IllegalArgumentException("field required");
        this.field = field;
        this.direction = direction == null ? Direction.ASC : direction;
    }

    public static SortOrder asc(String field) { return new SortOrder(field, Direction.ASC); }
    public static SortOrder desc(String field) { return new SortOrder(field, Direction.DESC); }

    public String getField() { return field; }
    public Direction getDirection() { return direction; }
}
