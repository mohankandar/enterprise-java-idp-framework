package io.github.mohankandar.idp.core.paging;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive tests for PageRequest.
 */
class PageRequestTest {

    @Test
    void createsPageRequestWithPageAndSize() {
        PageRequest req = PageRequest.of(0, 10);
        assertThat(req.getPage()).isEqualTo(0);
        assertThat(req.getSize()).isEqualTo(10);
        assertThat(req.getSort()).isEmpty();
    }

    @Test
    void calculatesOffsetCorrectly() {
        PageRequest req = PageRequest.of(2, 10);
        assertThat(req.offset()).isEqualTo(20);
    }

    @Test
    void offsetIsZeroForFirstPage() {
        PageRequest req = PageRequest.of(0, 10);
        assertThat(req.offset()).isEqualTo(0);
    }

    @Test
    void createsPageRequestWithSort() {
        List<SortOrder> sort = List.of(SortOrder.asc("name"), SortOrder.desc("createdAt"));
        PageRequest req = PageRequest.of(0, 20, sort);
        assertThat(req.getSort()).hasSize(2);
    }

    @Test
    void sortIsImmutable() {
        List<SortOrder> sort = List.of(SortOrder.asc("name"));
        PageRequest req = PageRequest.of(0, 10, sort);
        assertThatThrownBy(() -> req.getSort().add(SortOrder.asc("other")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void throwsOnNegativePage() {
        assertThatThrownBy(() -> PageRequest.of(-1, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("page must be >= 0");
    }

    @Test
    void throwsOnZeroSize() {
        assertThatThrownBy(() -> PageRequest.of(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("size must be > 0");
    }

    @Test
    void throwsOnNegativeSize() {
        assertThatThrownBy(() -> PageRequest.of(0, -5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("size must be > 0");
    }

    @Test
    void throwsOnNullSort() {
        assertThatThrownBy(() -> PageRequest.of(0, 10, null))
            .isInstanceOf(NullPointerException.class);
    }
}

