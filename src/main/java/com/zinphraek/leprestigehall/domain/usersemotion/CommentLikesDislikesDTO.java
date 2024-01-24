package com.zinphraek.leprestigehall.domain.usersemotion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentLikesDislikesDTO(
    Long id, @NotBlank String userId, @NotBlank String likeOrDislike, @NotNull Long commentId) {}
