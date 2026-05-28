package io.github.mohankandar.idp.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.AliasFor;

/**
 * Opinionated WebMvc slice test for IDP services.
 *
 * <p>Security filters are disabled by default via {@code addFilters=false} to keep controller tests focused.
 * If you need security enabled, use a plain {@link WebMvcTest} with {@link AutoConfigureMockMvc} configured as desired.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public @interface IdpWebMvcTest {

  @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
  Class<?>[] controllers() default {};

  @AliasFor(annotation = WebMvcTest.class, attribute = "properties")
  String[] properties() default {};
}
