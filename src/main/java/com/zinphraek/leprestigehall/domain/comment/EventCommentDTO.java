package com.zinphraek.leprestigehall.domain.comment;

import com.zinphraek.leprestigehall.domain.user.UserSummaryDTO;
import com.zinphraek.leprestigehall.domain.usersemotion.CommentLikesDislikes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventCommentDTO(
    Long id,
    @NotBlank @Size(message = "Must not exceed 2000", max = 2000) String content,
    @NotNull UserSummaryDTO user,
    @NotNull UUID eventId,
    Long basedCommentId,
    CommentLikesDislikes likesDislikes,
    @NotBlank LocalDateTime postedDate,
    boolean edited) {}
