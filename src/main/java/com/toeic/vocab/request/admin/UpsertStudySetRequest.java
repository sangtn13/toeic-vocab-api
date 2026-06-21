package com.toeic.vocab.request.admin;

import com.toeic.vocab.enums.StudySetStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertStudySetRequest(
    @NotBlank(message = "Study set title is required")
    @Size(max = 150, message = "Study set title must be at most 150 characters")
    String title,

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description,

    @Size(max = 500, message = "Thumbnail URL must be at most 500 characters")
    String thumbnailUrl,

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be non-negative")
    Integer displayOrder,

    @NotNull(message = "Study set status is required")
    StudySetStatus status
) {
}
