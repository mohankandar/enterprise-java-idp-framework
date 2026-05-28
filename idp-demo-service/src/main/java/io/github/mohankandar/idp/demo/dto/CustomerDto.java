package io.github.mohankandar.idp.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerDto(
    @NotBlank
    @Schema(example = "Ada")
    String firstName,

    @NotBlank
    @Schema(example = "Lovelace")
    String lastName,

    @Email
    @Schema(example = "ada.lovelace@example.com")
    String email
) {

}