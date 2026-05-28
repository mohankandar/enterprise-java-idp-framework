package io.github.mohankandar.idp.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isOkEnvelope;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@io.github.mohankandar.idp.test.annotations.IdpWebMvcTest(controllers = PingController.class)
class PingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingReturnsStandardIdpEnvelope() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(isOkEnvelope());
    }
}
