package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.demo.service.ExternalEchoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExternalEchoController {

  private final ExternalEchoService externalEchoService;

  public ExternalEchoController(ExternalEchoService externalEchoService) {
    this.externalEchoService = externalEchoService;
  }

  @GetMapping("/api/downstream/external/resttemplate")
  public ApiResponse<Object> callExternalWithRestTemplate() {
    return ApiResponse.ok(externalEchoService.callWithRestTemplate());
  }

  @GetMapping("/api/downstream/external/webclient")
  public ApiResponse<Object> callExternalWithWebClient() {
    return ApiResponse.ok(externalEchoService.callWithWebClient());
  }

  @GetMapping("/api/downstream/external/feign")
  public ApiResponse<Object> callExternalWithFeign() {
    return ApiResponse.ok(externalEchoService.callWithFeign());
  }
}
