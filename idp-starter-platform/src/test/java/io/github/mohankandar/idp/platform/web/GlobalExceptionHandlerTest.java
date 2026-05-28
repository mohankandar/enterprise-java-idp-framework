package io.github.mohankandar.idp.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.core.api.ErrorCode;
import io.github.mohankandar.idp.core.api.ErrorDetail;
import io.github.mohankandar.idp.core.error.IdpException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.hasCorrelationId;
import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isErrorEnvelope;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules()))
            .build();

    @Test
    void validationFailureReturnsBadRequestIdpEnvelopeAndCorrelationIdHeader() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(isErrorEnvelope(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(hasCorrelationId())
                .andExpect(jsonPath("$.error.details.fieldErrors.name").value("must not be blank"));
    }

    @Test
    void idpExceptionUsesMappedHttpStatusAndPreservesInboundCorrelationId() throws Exception {
        mockMvc.perform(get("/test/not-found")
                        .header("X-Correlation-Id", "corr-123"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Correlation-Id", "corr-123"))
                .andExpect(isErrorEnvelope(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.correlationId").value("corr-123"))
                .andExpect(jsonPath("$.error.message").value("Missing resource."));
    }

    @RestController
    static class TestController {
        @PostMapping("/test/validate")
        String validate(@Valid @RequestBody TestRequest request) {
            return "ok";
        }

        @GetMapping("/test/not-found")
        String notFound() {
            throw new IdpException(ErrorDetail.of(ErrorCode.NOT_FOUND, "Missing resource."), 404);
        }
    }

    static class TestRequest {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
