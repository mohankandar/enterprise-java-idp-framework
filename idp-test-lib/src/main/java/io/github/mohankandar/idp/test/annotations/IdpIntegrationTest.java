package io.github.mohankandar.idp.test.annotations;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opinionated full-context integration test for IDP services.
 *
 * <p>Activates the {@code test} profile by default. In IDP, test bootstrap remains console-only
 * for logging because the framework logging post-processor detects Spring test bootstrap and generic
 * JVM test runtime before Logback initializes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("it")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@ActiveProfiles("test")
public @interface IdpIntegrationTest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
  Class<?>[] classes() default {};

  @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
  String[] properties() default {};
}
