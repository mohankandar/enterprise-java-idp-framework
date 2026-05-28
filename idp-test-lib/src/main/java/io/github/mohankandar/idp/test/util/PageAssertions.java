package io.github.mohankandar.idp.test.util;

import org.assertj.core.api.Assertions;
import org.springframework.data.domain.Page;

/** Small helpers to keep Page assertions consistent across services. */
public final class PageAssertions {

  private PageAssertions() {}

  public static void assertPageMeta(Page<?> page, int expectedPageNumber, int expectedPageSize) {
    Assertions.assertThat(page).isNotNull();
    Assertions.assertThat(page.getNumber()).isEqualTo(expectedPageNumber);
    Assertions.assertThat(page.getSize()).isEqualTo(expectedPageSize);
  }
}
