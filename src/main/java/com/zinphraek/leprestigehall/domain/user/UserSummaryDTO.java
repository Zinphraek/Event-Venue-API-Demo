package com.zinphraek.leprestigehall.domain.user;

import jakarta.validation.constraints.NotBlank;

public record UserSummaryDTO(@NotBlank String userId, @NotBlank String firstName,
                             @NotBlank String lastName, String profilePictureUrl) {
}
