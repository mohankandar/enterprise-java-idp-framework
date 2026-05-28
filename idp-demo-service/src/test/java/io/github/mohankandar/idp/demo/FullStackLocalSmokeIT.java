package io.github.mohankandar.idp.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.hasCorrelationId;
import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isErrorEnvelope;
import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isOkEnvelope;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = DemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "idp.cache.enabled=false",
                "idp.data.redis.embedded.enabled=false",
                "spring.cloud.config.enabled=false",
                "idp.security.api-key.enabled=true",
                "idp.security.api-key.value=local-key"
        }
)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("local")
class FullStackLocalSmokeIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingReturnsEnvelopeCorrelationIdAndHeaderThroughRealFilterChain() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(isOkEnvelope())
                .andExpect(hasCorrelationId())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void securedEndpointRejectsMissingCredentialsWithStandardErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(isErrorEnvelope("UNAUTHORIZED"))
                .andExpect(hasCorrelationId())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void apiKeyAuthenticatedRequestCanReachProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("X-API-Key", "local-key"))
                .andExpect(status().isOk())
                .andExpect(isOkEnvelope())
                .andExpect(hasCorrelationId())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void openApiDocsEndpointRendersWithoutSpringMvcMethodErrors() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").isMap());
    }

    @Test
    void validationFailureStillCarriesCorrelationIdUnderRealFilters() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("X-API-Key", "local-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(isErrorEnvelope("VALIDATION_ERROR"))
                .andExpect(hasCorrelationId())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
