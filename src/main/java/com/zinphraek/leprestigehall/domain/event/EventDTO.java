package com.zinphraek.leprestigehall.domain.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.UUID;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

public record EventDTO(
    UUID id,
    boolean active, @NotNull @NotBlank String title,
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot be more than 2000 characters")
    String description,
    @DateTimeFormat(pattern = DATE_TIME_FORMAT) String postedDate) {
}
