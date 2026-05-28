package io.github.mohankandar.idp.core.paging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PageRequest {
    private final int page; // 0-based
    private final int size; // > 0
    private final List<SortOrder> sort;

    private PageRequest(int page, int size, List<SortOrder> sort) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        this.page = page;
        this.size = size;
        this.sort = Collections.unmodifiableList(new ArrayList<>(sort == null ? List.of() : sort));
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, List.of());
    }

    public static PageRequest of(int page, int size, List<SortOrder> sort) {
        Objects.requireNonNull(sort, "sort");
        return new PageRequest(page, size, sort);
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public List<SortOrder> getSort() { return sort; }

    public int offset() { return page * size; }
}
