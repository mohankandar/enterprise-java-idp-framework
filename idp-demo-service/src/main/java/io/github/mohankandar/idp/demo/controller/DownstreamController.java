package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.demo.client.echo.DownstreamEchoClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DownstreamController {
    private final DownstreamEchoClient client;
    public DownstreamController(DownstreamEchoClient client) { this.client = client; }

    @GetMapping("/api/downstream/ping")
    public ApiResponse<Object> downstream() {
        return ApiResponse.ok(client.callEcho());
    }
}
