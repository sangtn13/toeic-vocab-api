package com.toeic.vocab.request.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertStudyUnitRequest(
    @NotBlank(message = "Unit title is required")
    @Size(max = 150, message = "Unit title must be at most 150 characters")
    String title,

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description,

    @NotNull(message = "Unit order is required")
    @Min(value = 1, message = "Unit order must be at least 1")
    Integer unitOrder,

    @NotNull(message = "Active flag is required")
    Boolean active
) {
}
