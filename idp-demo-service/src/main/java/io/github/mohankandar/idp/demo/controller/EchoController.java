package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.core.api.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Internal debug endpoint used by downstream and external echo calls.
 * Echoes back selected headers (Authorization, X-API-Key) wrapped in ApiResponse.
 */
@RestController
@RequestMapping(value = "/internal", produces = MediaType.APPLICATION_JSON_VALUE)
public class EchoController {

  @GetMapping("/echo")
  public ApiResponse<Map<String, String>> echo(@RequestHeader HttpHeaders headers) {
    Map<String, String> body = Map.of(
        "authorization", String.valueOf(headers.getFirst(HttpHeaders.AUTHORIZATION)),
        "x-api-key", String.valueOf(headers.getFirst("X-API-Key"))
    );
    return ApiResponse.ok(body);
  }
}
