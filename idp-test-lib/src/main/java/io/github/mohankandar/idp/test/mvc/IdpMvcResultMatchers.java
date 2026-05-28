package io.github.mohankandar.idp.test.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Common MockMvc {@link ResultMatcher}s for the IDP {@code ApiResponse} envelope.
 *
 * <p>Envelope contract (idp-core):
 * <ul>
 *   <li>Success: status="ok", data != null</li>
 *   <li>Error: status="error", error != null</li>
 * </ul>
 */
public final class IdpMvcResultMatchers {

  private IdpMvcResultMatchers() {}

  public static ResultMatcher isOkEnvelope() {
    return result -> {
      MockMvcResultMatchers.jsonPath("$.status").value("ok").match(result);
      MockMvcResultMatchers.jsonPath("$.data").exists().match(result);
    };
  }

  public static ResultMatcher isErrorEnvelope() {
    return result -> {
      MockMvcResultMatchers.jsonPath("$.status").value("error").match(result);
      MockMvcResultMatchers.jsonPath("$.error").exists().match(result);
      MockMvcResultMatchers.jsonPath("$.error.message").exists().match(result);
    };
  }

  public static ResultMatcher isErrorEnvelope(String errorCode) {
    return result -> {
      isErrorEnvelope().match(result);
      MockMvcResultMatchers.jsonPath("$.error.code").value(errorCode).match(result);
    };
  }

  public static ResultMatcher hasCorrelationId() {
    return result -> MockMvcResultMatchers.jsonPath("$.correlationId").isNotEmpty().match(result);
  }

  public static ResultMatcher hasHttpStatus(HttpStatus status) {
    return MockMvcResultMatchers.status().is(status.value());
  }
}