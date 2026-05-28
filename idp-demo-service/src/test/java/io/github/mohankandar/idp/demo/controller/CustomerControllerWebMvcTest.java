package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.demo.domain.Customer;
import io.github.mohankandar.idp.demo.service.CustomerService;
import io.github.mohankandar.idp.platform.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isErrorEnvelope;
import static io.github.mohankandar.idp.test.mvc.IdpMvcResultMatchers.isOkEnvelope;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@io.github.mohankandar.idp.test.annotations.IdpWebMvcTest(controllers = CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void searchReturnsOkEnvelopeWithPagedData() throws Exception {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com");
        when(customerService.search(eq("Ada"), eq(null), eq(null), eq(0), eq(10), eq("createdDate,desc")))
                .thenReturn(new PageImpl<>(List.of(customer), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/customers").param("name", "Ada"))
                .andExpect(status().isOk())
                .andExpect(isOkEnvelope())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Ada"));
    }

    @Test
    void createValidationFailureReturnsStandardErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(isErrorEnvelope("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.error.details.fieldErrors.lastName").exists());
    }

    @Test
    void getByIdReturnsOkEnvelopeWhenCustomerExists() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer("Grace", "Hopper", "grace@example.com");
        when(customerService.findById(id)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(isOkEnvelope())
                .andExpect(jsonPath("$.data.email").value("grace@example.com"));
    }
}
