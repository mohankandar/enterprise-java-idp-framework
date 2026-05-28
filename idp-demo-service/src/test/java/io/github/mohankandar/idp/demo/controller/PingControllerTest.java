package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.core.api.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PingControllerTest {

    private final PingController controller = new PingController();

    @Test
    void pingReturnsSuccessfulResponse() {
        ApiResponse<String> response = controller.ping();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ok");
        assertThat(response.getData()).isEqualTo("pong");
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
}
