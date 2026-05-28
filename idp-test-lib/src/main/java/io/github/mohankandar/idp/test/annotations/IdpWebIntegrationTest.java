package io.github.mohankandar.idp.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

/**
 * Opinionated IDP web integration test for controller + real service collaboration inside a
 * controlled MVC slice.
 *
 * <p>This standard avoids full application startup fragility. Consumer tests should target one
 * or more controllers, import the real service beans under test, and mock infrastructure
 * boundaries such as repositories, caches, and external clients.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("it")
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public @interface IdpWebIntegrationTest {

  @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
  Class<?>[] controllers() default {};

  @AliasFor(annotation = WebMvcTest.class, attribute = "properties")
  String[] properties() default {};
}
