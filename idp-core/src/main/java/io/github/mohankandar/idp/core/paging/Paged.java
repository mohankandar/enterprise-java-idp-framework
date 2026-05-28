package io.github.mohankandar.idp.core.paging;

import java.util.Collections;
import java.util.List;

public final class Paged<T> {
    private final List<T> items;
    private final int page;
    private final int size;
    private final long totalElements;

    private Paged(List<T> items, int page, int size, long totalElements) {
        this.items = Collections.unmodifiableList(items == null ? List.of() : items);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements < 0 ? 0 : totalElements;
    }

    public static <T> Paged<T> of(List<T> items, int page, int size, long totalElements) {
        return new Paged<>(items, page, size, totalElements);
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }

    public long getTotalPages() {
        if (size <= 0) return 0;
        return (totalElements + size - 1) / size;
    }

    public boolean isFirst() { return page <= 0; }
    public boolean isLast() { return page + 1 >= getTotalPages(); }
    public boolean hasNext() { return !isLast(); }
    public boolean hasPrev() { return page > 0; }
}
