package com.zinphraek.leprestigehall.domain.review;

import com.zinphraek.leprestigehall.domain.user.UserSummaryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewDTO(Long id, @NotBlank String title, @NotNull Long rating,
                        @NotBlank @Size(message = "Must not exceed 2000", max = 2000) String comment,
                        @NotBlank String postedDate, String lastEditedDate,
                        @NotNull UserSummaryDTO user) {

}
