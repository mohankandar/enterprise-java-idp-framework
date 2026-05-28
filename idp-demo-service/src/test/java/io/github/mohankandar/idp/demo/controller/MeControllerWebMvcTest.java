package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.identity.IdpIdentityClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isErrorEnvelope;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@io.github.mohankandar.idp.test.annotations.IdpWebMvcTest(controllers = MeController.class)
class MeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdpIdentityClient identityClient;

    @Test
    void meReturnsUnauthorizedErrorEnvelopeWhenIdentityMissing() throws Exception {
        when(identityClient.current()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(isErrorEnvelope("UNAUTHORIZED"));
    }
}
