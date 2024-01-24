package com.zinphraek.leprestigehall.domain.usersemotion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EventLikesDislikesDTO(
    Long id, @NotBlank String userId, @NotBlank String likeOrDislike, @NotNull UUID eventId) {}
