package io.github.mohankandar.idp.core.paging;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for Paged - the paginated response wrapper.
 */
class PagedTest {

    @Test
    void createsPagedWithItems() {
        Paged<String> paged = Paged.of(List.of("a", "b", "c"), 0, 10, 3);
        assertThat(paged.getItems()).containsExactly("a", "b", "c");
        assertThat(paged.getTotalElements()).isEqualTo(3);
    }

    @Test
    void calculatesTotalPages() {
        Paged<String> paged = Paged.of(List.of(), 0, 10, 25);
        assertThat(paged.getTotalPages()).isEqualTo(3); // 25/10 = 2.5 -> 3 pages
    }

    @Test
    void totalPagesRoundsUp() {
        Paged<String> paged = Paged.of(List.of(), 0, 10, 21);
        assertThat(paged.getTotalPages()).isEqualTo(3);
    }

    @Test
    void isFirstOnPageZero() {
        Paged<String> paged = Paged.of(List.of(), 0, 10, 50);
        assertThat(paged.isFirst()).isTrue();
        assertThat(paged.hasPrev()).isFalse();
    }

    @Test
    void isNotFirstOnLaterPages() {
        Paged<String> paged = Paged.of(List.of(), 2, 10, 50);
        assertThat(paged.isFirst()).isFalse();
        assertThat(paged.hasPrev()).isTrue();
    }

    @Test
    void isLastOnFinalPage() {
        Paged<String> paged = Paged.of(List.of(), 4, 10, 50);
        assertThat(paged.isLast()).isTrue();
        assertThat(paged.hasNext()).isFalse();
    }

    @Test
    void isNotLastWhenMorePagesExist() {
        Paged<String> paged = Paged.of(List.of(), 0, 10, 50);
        assertThat(paged.isLast()).isFalse();
        assertThat(paged.hasNext()).isTrue();
    }

    @Test
    void handlesNegativeTotalElementsAsZero() {
        Paged<String> paged = Paged.of(List.of(), 0, 10, -1);
        assertThat(paged.getTotalElements()).isEqualTo(0);
    }

    @Test
    void handlesNullItemsAsEmptyList() {
        Paged<String> paged = Paged.of(null, 0, 10, 0);
        assertThat(paged.getItems()).isEmpty();
    }

    @Test
    void itemsListIsImmutable() {
        Paged<String> paged = Paged.of(List.of("a", "b"), 0, 10, 2);
        assertThat(paged.getItems())
            .isUnmodifiable();
    }

    @Test
    void singlePageHasNoNextOrPrev() {
        Paged<String> paged = Paged.of(List.of("x"), 0, 10, 1);
        assertThat(paged.isFirst()).isTrue();
        assertThat(paged.isLast()).isTrue();
        assertThat(paged.hasNext()).isFalse();
        assertThat(paged.hasPrev()).isFalse();
    }
}

