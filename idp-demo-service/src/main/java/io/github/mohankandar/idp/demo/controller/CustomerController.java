package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.core.api.ErrorCode;
import io.github.mohankandar.idp.core.api.ErrorDetail;
import io.github.mohankandar.idp.demo.domain.Customer;
import io.github.mohankandar.idp.demo.dto.CustomerDto;
import io.github.mohankandar.idp.demo.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

  private final CustomerService service;

  public CustomerController(CustomerService service) {
    this.service = service;
  }

  // -------- SEARCH --------
  @Operation(summary = "Search customers with optional filters and paging")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Search results returned")
  })
  @GetMapping
  public ApiResponse<Page<Customer>> search(
      @Parameter(description = "Exact first/last name match")
      @RequestParam(required = false) String name,
      @Parameter(description = "Substring that must be contained in email")
      @RequestParam(required = false) String emailContains,
      @Parameter(description = "Only customers created after this timestamp (UTC)")
      @RequestParam(required = false) Instant createdAfter,
      @Parameter(description = "Zero-based page index")
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Page size")
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @Parameter(description = "Sort expression, e.g. 'createdDate,desc'")
      @RequestParam(required = false, defaultValue = "createdDate,desc") String sort) {

    return ApiResponse.ok(service.search(name, emailContains, createdAfter, page, size, sort));
  }

  // -------- CREATE --------
  @Operation(summary = "Create a customer")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Customer created"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Validation error",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<Customer> create(@RequestBody @Valid CustomerDto dto) {
    return ApiResponse.ok(service.create(dto));
  }

  // -------- GET BY ID --------
  @Operation(summary = "Get a customer by ID")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Customer found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "Customer not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Customer>> get(
      @Parameter(description = "Customer ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
      @PathVariable("id") UUID id) {

    return service.findById(id)
        .map(c -> ResponseEntity.ok(ApiResponse.ok(c)))
        .orElseGet(() -> {
          ErrorDetail error = ErrorDetail.of(
              ErrorCode.NOT_FOUND,
              "Customer not found.",
              java.util.Map.of("id", String.valueOf(id))
          );
          return ResponseEntity.status(404).body(ApiResponse.error(error));
        });
  }

  // -------- UPDATE --------
  @Operation(summary = "Update a customer")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Customer updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "Validation error",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "Customer not found")
  })
  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResponse<Customer>> update(
      @PathVariable("id") UUID id,
      @RequestBody @Valid CustomerDto dto) {

    var updated = service.update(id, dto); // assume throws NotFound → global 404
    return ResponseEntity.ok(ApiResponse.ok(updated));
  }

  // -------- DELETE --------
  @Operation(summary = "Delete a customer (admin only)")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "204",
          description = "Customer deleted"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "Forbidden"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "Customer not found")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.delete(id); // assume throws NotFound → global 404
    return ResponseEntity.noContent().build(); // 204 No Content
  }
}
