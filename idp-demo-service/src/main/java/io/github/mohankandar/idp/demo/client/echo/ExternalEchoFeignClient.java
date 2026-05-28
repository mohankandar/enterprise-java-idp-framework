package io.github.mohankandar.idp.demo.client.echo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Simple Feign client that calls an external echo endpoint.
 * Default base URL is https://httpbin.org but can be overridden via config.
 */
@FeignClient(
    name = "externalEchoFeign",
    url = "${idp.partners.services.external-echo.rest.baseUrl:https://httpbin.org}"
)
public interface ExternalEchoFeignClient {

  @GetMapping("/anything/feign")
  Map<String, Object> echo(@RequestParam("source") String source);
}
